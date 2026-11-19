package com.railflow.inventory.service;

import com.railflow.common.enums.BerthType;
import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.InventoryStatus;
import com.railflow.exception.BookingException;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.inventory.dto.*;
import com.railflow.inventory.entity.SeatHold;
import com.railflow.inventory.entity.SeatInventory;
import com.railflow.inventory.repository.SeatHoldRepository;
import com.railflow.inventory.repository.SeatInventoryRepository;
import com.railflow.station.entity.Station;
import com.railflow.station.repository.StationRepository;
import com.railflow.train.entity.Coach;
import com.railflow.train.entity.Seat;
import com.railflow.train.entity.Train;
import com.railflow.train.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatInventoryService {

    private final SeatInventoryRepository seatInventoryRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final RedissonClient redissonClient;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TransactionTemplate transactionTemplate;

    public SeatHoldResponse holdSeats(HoldSeatsRequest request) {
        String lockKey = "seat-lock:" + request.getTrainId() + ":" + request.getJourneyDate() + ":" + request.getCoachType();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BookingException("Could not acquire seat lock, please retry");
            }

            // Execute DB operations inside a managed transaction before releasing lock
            return transactionTemplate.execute(status -> executeHoldSeats(request));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BookingException("Interrupted while acquiring seat lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private SeatHoldResponse executeHoldSeats(HoldSeatsRequest request) {
        // Ensure inventory exists
        if (!seatInventoryRepository.existsByTrainIdAndJourneyDateAndSourceStationIdAndDestinationStationId(
                request.getTrainId(), request.getJourneyDate(), request.getSourceStationId(), request.getDestStationId())) {
            initializeInventory(request.getTrainId(), request.getJourneyDate(), request.getSourceStationId(), request.getDestStationId());
        }

        List<SeatInventory> availableSeats = seatInventoryRepository.findAvailableSeats(
                request.getTrainId(),
                request.getJourneyDate(),
                request.getCoachType(),
                request.getSourceStationId(),
                request.getDestStationId()
        );

        if (availableSeats.size() < request.getPassengerCount()) {
            throw new BookingException("Not enough seats available. Requested: " +
                    request.getPassengerCount() + ", Available: " + availableSeats.size());
        }

        // Allocate seats, respecting berth preferences when possible
        List<SeatInventory> selectedSeats = new ArrayList<>();
        List<SeatInventory> pool = new ArrayList<>(availableSeats);

        if (request.getBerthPreferences() != null && !request.getBerthPreferences().isEmpty()) {
            for (BerthType pref : request.getBerthPreferences()) {
                if (selectedSeats.size() >= request.getPassengerCount()) {
                    break;
                }
                Optional<SeatInventory> match = pool.stream()
                        .filter(s -> s.getSeat() != null && s.getSeat().getBerthType() == pref)
                        .findFirst();
                if (match.isPresent()) {
                    SeatInventory chosen = match.get();
                    selectedSeats.add(chosen);
                    pool.remove(chosen);
                }
            }
        }

        // Fill remaining required seats
        while (selectedSeats.size() < request.getPassengerCount() && !pool.isEmpty()) {
            selectedSeats.add(pool.remove(0));
        }

        LocalDateTime holdExpiry = LocalDateTime.now().plusMinutes(10);
        for (SeatInventory seat : selectedSeats) {
            seat.setStatus(InventoryStatus.HELD);
            seat.setHoldExpiry(holdExpiry);
        }
        seatInventoryRepository.saveAll(selectedSeats);

        String holdToken = UUID.randomUUID().toString();
        List<Long> inventoryIds = selectedSeats.stream().map(SeatInventory::getId).collect(Collectors.toList());

        SeatHold seatHold = SeatHold.builder()
                .holdToken(holdToken)
                .inventoryIds(inventoryIds)
                .userId(request.getUserId() != null ? request.getUserId() : 0L)
                .journeyDate(request.getJourneyDate())
                .trainId(request.getTrainId())
                .sourceStationId(request.getSourceStationId())
                .destStationId(request.getDestStationId())
                .coachType(request.getCoachType())
                .expiresAt(holdExpiry)
                .isReleased(false)
                .build();
        seatHoldRepository.save(seatHold);

        BigDecimal totalFare = selectedSeats.stream()
                .map(SeatInventory::getFare)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SeatInfo> seatInfoList = selectedSeats.stream()
                .map(s -> SeatInfo.builder()
                        .inventoryId(s.getId())
                        .seatId(s.getSeat() != null ? s.getSeat().getId() : null)
                        .coachNumber(s.getCoach() != null ? s.getCoach().getCoachNumber() : "")
                        .seatNumber(s.getSeat() != null ? s.getSeat().getSeatNumber() : 0)
                        .berthType(s.getSeat() != null ? s.getSeat().getBerthType() : null)
                        .coachType(s.getCoachType())
                        .fare(s.getFare())
                        .build())
                .collect(Collectors.toList());

        log.info("Held {} seats under token {} for train {} on {}",
                selectedSeats.size(), holdToken, request.getTrainId(), request.getJourneyDate());

        return SeatHoldResponse.builder()
                .holdToken(holdToken)
                .seats(seatInfoList)
                .expiresAt(holdExpiry)
                .totalFare(totalFare)
                .build();
    }

    @Transactional
    public void confirmHold(String holdToken, Long bookingId) {
        SeatHold seatHold = seatHoldRepository.findByHoldToken(holdToken)
                .orElseThrow(() -> new BookingException("Seat hold not found for token: " + holdToken));

        if (seatHold.isReleased()) {
            throw new BookingException("Seat hold has already been released or confirmed");
        }
        if (seatHold.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BookingException("Seat hold has expired");
        }

        List<SeatInventory> items = seatInventoryRepository.findAllById(seatHold.getInventoryIds());
        for (SeatInventory item : items) {
            item.setStatus(InventoryStatus.BOOKED);
            item.setBookingId(bookingId);
            item.setHoldExpiry(null);
        }
        seatInventoryRepository.saveAll(items);

        seatHold.setReleased(true);
        seatHoldRepository.save(seatHold);
        log.info("Confirmed hold {} for booking id {}", holdToken, bookingId);
    }

    @Transactional
    public void releaseHold(String holdToken) {
        SeatHold seatHold = seatHoldRepository.findByHoldToken(holdToken)
                .orElseThrow(() -> new BookingException("Seat hold not found for token: " + holdToken));

        if (seatHold.isReleased()) {
            log.warn("Seat hold {} was already marked released", holdToken);
            return;
        }

        List<SeatInventory> items = seatInventoryRepository.findAllById(seatHold.getInventoryIds());
        for (SeatInventory item : items) {
            if (item.getStatus() == InventoryStatus.HELD) {
                item.setStatus(InventoryStatus.AVAILABLE);
                item.setHoldExpiry(null);
                item.setBookingId(null);
            }
        }
        seatInventoryRepository.saveAll(items);

        seatHold.setReleased(true);
        seatHoldRepository.save(seatHold);
        log.info("Released hold {} with {} seats back to inventory", holdToken, items.size());
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        int releasedSeats = seatInventoryRepository.releaseExpiredHolds(now);

        List<SeatHold> expiredHolds = seatHoldRepository.findByExpiresAtBeforeAndIsReleasedFalse(now);
        for (SeatHold hold : expiredHolds) {
            hold.setReleased(true);
        }
        seatHoldRepository.saveAll(expiredHolds);

        if (releasedSeats > 0 || !expiredHolds.isEmpty()) {
            log.info("Released {} expired inventory seats and marked {} holds released",
                    releasedSeats, expiredHolds.size());
        }
    }

    @Transactional
    public void initializeInventory(Long trainId, LocalDate journeyDate, Long srcStationId, Long destStationId) {
        if (seatInventoryRepository.existsByTrainIdAndJourneyDateAndSourceStationIdAndDestinationStationId(
                trainId, journeyDate, srcStationId, destStationId)) {
            return;
        }

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + trainId));

        Station srcStation = stationRepository.findById(srcStationId)
                .orElseThrow(() -> new ResourceNotFoundException("Source station not found with id: " + srcStationId));

        Station destStation = stationRepository.findById(destStationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found with id: " + destStationId));

        List<Coach> coaches = train.getCoaches();
        if (coaches == null || coaches.isEmpty()) {
            log.warn("Train {} has no coaches configured, skipping inventory initialization", trainId);
            return;
        }

        Integer distance = train.getTotalDistance() != null ? train.getTotalDistance() : 500;
        List<SeatInventory> inventoryList = new ArrayList<>();

        for (Coach coach : coaches) {
            if (!coach.isActive()) continue;
            BigDecimal fare = calculateFare(coach.getCoachType(), distance);

            List<Seat> seats = coach.getSeats();
            if (seats != null) {
                for (Seat seat : seats) {
                    if (!seat.isActive()) continue;
                    SeatInventory item = SeatInventory.builder()
                            .train(train)
                            .coach(coach)
                            .seat(seat)
                            .sourceStation(srcStation)
                            .destinationStation(destStation)
                            .journeyDate(journeyDate)
                            .coachType(coach.getCoachType())
                            .status(InventoryStatus.AVAILABLE)
                            .fare(fare)
                            .build();
                    inventoryList.add(item);
                }
            }
        }

        if (!inventoryList.isEmpty()) {
            seatInventoryRepository.saveAll(inventoryList);
            log.info("Initialized {} seats in inventory for train {} on {} from {} to {}",
                    inventoryList.size(), trainId, journeyDate, srcStation.getStationCode(), destStation.getStationCode());
        }
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long trainId, LocalDate journeyDate, CoachType coachType,
                                               Long srcStationId, Long destStationId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + trainId));

        Long effectiveSrcId = srcStationId != null ? srcStationId :
                (train.getSourceStation() != null ? train.getSourceStation().getId() : null);
        Long effectiveDestId = destStationId != null ? destStationId :
                (train.getDestinationStation() != null ? train.getDestinationStation().getId() : null);

        if (effectiveSrcId == null || effectiveDestId == null) {
            throw new BookingException("Source and destination stations could not be resolved for train " + trainId);
        }

        if (!seatInventoryRepository.existsByTrainIdAndJourneyDateAndSourceStationIdAndDestinationStationId(
                trainId, journeyDate, effectiveSrcId, effectiveDestId)) {
            initializeInventory(trainId, journeyDate, effectiveSrcId, effectiveDestId);
        }

        List<SeatInventory> seats = seatInventoryRepository
                .findByTrainIdAndJourneyDateAndCoachTypeAndSourceStationIdAndDestinationStationId(
                        trainId, journeyDate, coachType, effectiveSrcId, effectiveDestId);

        Map<String, List<SeatInventory>> coachMap = seats.stream()
                .filter(s -> s.getCoach() != null)
                .collect(Collectors.groupingBy(s -> s.getCoach().getCoachNumber()));

        List<CoachAvailability> coaches = coachMap.entrySet().stream()
                .map(entry -> {
                    String coachNumber = entry.getKey();
                    List<SeatInventory> list = entry.getValue();
                    int available = (int) list.stream()
                            .filter(s -> s.getStatus() == InventoryStatus.AVAILABLE)
                            .count();
                    int total = list.size();
                    return CoachAvailability.builder()
                            .coachNumber(coachNumber)
                            .coachType(coachType)
                            .availableSeats(available)
                            .totalSeats(total)
                            .build();
                })
                .sorted(Comparator.comparing(CoachAvailability::getCoachNumber))
                .collect(Collectors.toList());

        long availableCount = coaches.stream().mapToInt(CoachAvailability::getAvailableSeats).sum();
        BigDecimal fare = seats.isEmpty() ? calculateFare(coachType, train.getTotalDistance()) : seats.get(0).getFare();

        return AvailabilityResponse.builder()
                .trainId(trainId)
                .journeyDate(journeyDate)
                .coachType(coachType)
                .availableCount(availableCount)
                .fare(fare)
                .coaches(coaches)
                .build();
    }

    @Transactional(readOnly = true)
    public SeatHoldResponse getHoldDetails(String holdToken) {
        SeatHold seatHold = seatHoldRepository.findByHoldToken(holdToken)
                .orElseThrow(() -> new BookingException("Seat hold not found for token: " + holdToken));

        if (seatHold.isReleased()) {
            throw new BookingException("Seat hold has already been released or confirmed");
        }
        if (seatHold.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BookingException("Seat hold has expired");
        }

        List<SeatInventory> items = seatInventoryRepository.findAllById(seatHold.getInventoryIds());
        List<SeatInfo> seatInfoList = items.stream()
                .map(s -> SeatInfo.builder()
                        .inventoryId(s.getId())
                        .seatId(s.getSeat() != null ? s.getSeat().getId() : null)
                        .coachNumber(s.getCoach() != null ? s.getCoach().getCoachNumber() : "")
                        .seatNumber(s.getSeat() != null ? s.getSeat().getSeatNumber() : 0)
                        .berthType(s.getSeat() != null ? s.getSeat().getBerthType() : null)
                        .coachType(s.getCoachType())
                        .fare(s.getFare())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalFare = items.stream()
                .map(SeatInventory::getFare)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SeatHoldResponse.builder()
                .holdToken(holdToken)
                .seats(seatInfoList)
                .expiresAt(seatHold.getExpiresAt())
                .totalFare(totalFare)
                .build();
    }

    public BigDecimal calculateFare(CoachType coachType, Integer distanceKm) {
        int distance = (distanceKm != null && distanceKm > 0) ? distanceKm : 500;
        double distFactor = Math.max(1.0, distance / 100.0);
        double multiplier = switch (coachType) {
            case ONE_A -> 4.5;
            case TWO_A -> 3.0;
            case THREE_A -> 2.0;
            case EC -> 2.5;
            case CC -> 1.5;
            case SL -> 1.0;
            case GEN -> 0.5;
        };
        double fare = 120.0 * distFactor * multiplier;
        return BigDecimal.valueOf(Math.round(fare * 100.0) / 100.0).setScale(2, RoundingMode.HALF_UP);
    }
}
