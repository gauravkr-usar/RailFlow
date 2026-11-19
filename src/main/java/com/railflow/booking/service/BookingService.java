package com.railflow.booking.service;

import com.railflow.booking.dto.*;
import com.railflow.booking.entity.Booking;
import com.railflow.booking.entity.BookingPassenger;
import com.railflow.booking.event.BookingCancelledEvent;
import com.railflow.booking.event.BookingConfirmedEvent;
import com.railflow.booking.event.BookingCreatedEvent;
import com.railflow.booking.repository.BookingPassengerRepository;
import com.railflow.booking.repository.BookingRepository;
import com.railflow.common.enums.BookingStatus;
import com.railflow.common.enums.PassengerStatus;
import com.railflow.exception.BookingException;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.inventory.dto.HoldSeatsRequest;
import com.railflow.inventory.dto.SeatHoldResponse;
import com.railflow.inventory.dto.SeatInfo;
import com.railflow.inventory.service.SeatInventoryService;
import com.railflow.passenger.entity.Passenger;
import com.railflow.passenger.repository.PassengerRepository;
import com.railflow.station.entity.Station;
import com.railflow.station.repository.StationRepository;
import com.railflow.train.entity.Train;
import com.railflow.train.repository.TrainRepository;
import com.railflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final PassengerRepository passengerRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final SeatInventoryService seatInventoryService;
    private final PnrGenerator pnrGenerator;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, User user) {
        log.info("Processing booking request for user {} on train {}", user.getEmail(), request.getTrainId());

        // 1. Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Duplicate booking request detected with key: {}. Returning existing booking.", request.getIdempotencyKey());
                return mapToBookingResponse(existing.get());
            }
        }

        // 2. Validate Train & Stations
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + request.getTrainId()));

        Station sourceStation = stationRepository.findByStationCode(request.getSourceStationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Source station not found: " + request.getSourceStationCode()));

        Station destStation = stationRepository.findByStationCode(request.getDestinationStationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found: " + request.getDestinationStationCode()));

        // 3. Acquire seat holds
        SeatHoldResponse holdResponse;
        String holdToken = request.getHoldToken();
        if (holdToken == null || holdToken.isBlank()) {
            HoldSeatsRequest holdRequest = HoldSeatsRequest.builder()
                    .trainId(train.getId())
                    .sourceStationId(sourceStation.getId())
                    .destStationId(destStation.getId())
                    .journeyDate(request.getJourneyDate())
                    .coachType(request.getCoachType())
                    .passengerCount(request.getPassengers().size())
                    .userId(user.getId())
                    .berthPreferences(request.getPassengers().stream()
                            .map(PassengerRequest::getBerthPreference)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toList()))
                    .build();
            holdResponse = seatInventoryService.holdSeats(holdRequest);
            holdToken = holdResponse.getHoldToken();
        } else {
            holdResponse = seatInventoryService.getHoldDetails(holdToken);
        }

        // 4. Generate unique PNR and create Booking entity
        String pnr = pnrGenerator.generate();
        BigDecimal totalFare = holdResponse.getTotalFare();

        Booking booking = Booking.builder()
                .pnr(pnr)
                .user(user)
                .train(train)
                .sourceStation(sourceStation)
                .destinationStation(destStation)
                .journeyDate(request.getJourneyDate())
                .coachType(request.getCoachType())
                .status(BookingStatus.PAYMENT_PENDING)
                .totalFare(totalFare)
                .holdToken(holdToken)
                .idempotencyKey(request.getIdempotencyKey())
                .bookingPassengers(new ArrayList<>())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 5. Create Passengers and assign seat info from hold
        List<SeatInfo> allocatedSeats = holdResponse.getSeats();
        List<BookingPassenger> bookingPassengers = new ArrayList<>();

        for (int i = 0; i < request.getPassengers().size(); i++) {
            PassengerRequest pr = request.getPassengers().get(i);
            Passenger passenger = Passenger.builder()
                    .user(user)
                    .name(pr.getName())
                    .age(pr.getAge())
                    .gender(pr.getGender())
                    .idProofType(pr.getIdProofType())
                    .idProofNumber(pr.getIdProofNumber())
                    .isActive(true)
                    .build();
            Passenger savedPassenger = passengerRepository.save(passenger);

            SeatInfo seatInfo = (allocatedSeats != null && i < allocatedSeats.size()) ? allocatedSeats.get(i) : null;

            BookingPassenger bp = BookingPassenger.builder()
                    .booking(savedBooking)
                    .passenger(savedPassenger)
                    .seatInventoryId(seatInfo != null ? seatInfo.getInventoryId() : null)
                    .coachNumber(seatInfo != null ? seatInfo.getCoachNumber() : "WL")
                    .seatNumber(seatInfo != null ? seatInfo.getSeatNumber() : 0)
                    .berthType(seatInfo != null ? seatInfo.getBerthType() : pr.getBerthPreference())
                    .status(seatInfo != null ? PassengerStatus.CONFIRMED : PassengerStatus.WAITING_LIST)
                    .fare(seatInfo != null && seatInfo.getFare() != null ? seatInfo.getFare() : totalFare.divide(BigDecimal.valueOf(request.getPassengers().size()), 2, java.math.RoundingMode.HALF_UP))
                    .build();

            bookingPassengers.add(bp);
        }

        bookingPassengerRepository.saveAll(bookingPassengers);
        savedBooking.setBookingPassengers(bookingPassengers);

        // 6. Publish BookingCreatedEvent
        publishBookingCreatedEvent(savedBooking, user);

        return mapToBookingResponse(savedBooking);
    }

    @Transactional
    @CacheEvict(value = "pnrCache", key = "#pnr")
    public BookingResponse confirmBookingByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with PNR: " + pnr));
        return confirmBooking(booking.getId());
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.info("Booking {} already confirmed", bookingId);
            return mapToBookingResponse(booking);
        }

        // Confirm inventory hold
        if (booking.getHoldToken() != null && !booking.getHoldToken().isBlank()) {
            seatInventoryService.confirmHold(booking.getHoldToken(), booking.getId());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updated = bookingRepository.save(booking);

        // Publish event
        publishBookingConfirmedEvent(updated);

        log.info("Booking {} confirmed successfully with PNR {}", bookingId, updated.getPnr());
        return mapToBookingResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "pnrCache", key = "#booking.pnr")
    public void cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findByIdAndUser(bookingId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or does not belong to user"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        // Release inventory
        if (booking.getHoldToken() != null && !booking.getHoldToken().isBlank()) {
            try {
                seatInventoryService.releaseHold(booking.getHoldToken());
            } catch (Exception e) {
                log.warn("Error releasing hold for cancelled booking: {}", e.getMessage());
            }
        }

        // Update passenger statuses
        if (booking.getBookingPassengers() != null) {
            for (BookingPassenger bp : booking.getBookingPassengers()) {
                bp.setStatus(PassengerStatus.CANCELLED);
            }
            bookingPassengerRepository.saveAll(booking.getBookingPassengers());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Publish event
        publishBookingCancelledEvent(booking);
        log.info("Booking {} cancelled successfully", bookingId);
    }

    @Cacheable(value = "pnrCache", key = "#pnr", unless = "#result == null")
    @Transactional(readOnly = true)
    public PnrStatusResponse getBookingByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with PNR: " + pnr));

        List<PassengerBookingInfo> passengers = booking.getBookingPassengers().stream()
                .map(this::mapToPassengerInfo)
                .collect(Collectors.toList());

        return PnrStatusResponse.builder()
                .pnr(booking.getPnr())
                .status(booking.getStatus())
                .trainNumber(booking.getTrain().getTrainNumber())
                .trainName(booking.getTrain().getTrainName())
                .sourceStationCode(booking.getSourceStation().getStationCode())
                .sourceStationName(booking.getSourceStation().getStationName())
                .destinationStationCode(booking.getDestinationStation().getStationCode())
                .destinationStationName(booking.getDestinationStation().getStationName())
                .journeyDate(booking.getJourneyDate())
                .departureTime(booking.getTrain().getDepartureTime())
                .arrivalTime(booking.getTrain().getArrivalTime())
                .coachType(booking.getCoachType())
                .totalFare(booking.getTotalFare())
                .paymentStatus(booking.getPayment() != null ? booking.getPayment().getStatus().name() : "PENDING")
                .passengers(passengers)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToBookingResponse(booking);
    }

    public BookingResponse mapToBookingResponse(Booking b) {
        List<PassengerBookingInfo> passengerInfos = (b.getBookingPassengers() != null)
                ? b.getBookingPassengers().stream().map(this::mapToPassengerInfo).collect(Collectors.toList())
                : List.of();

        return BookingResponse.builder()
                .bookingId(b.getId())
                .pnr(b.getPnr())
                .status(b.getStatus())
                .trainId(b.getTrain() != null ? b.getTrain().getId() : null)
                .trainNumber(b.getTrain() != null ? b.getTrain().getTrainNumber() : "")
                .trainName(b.getTrain() != null ? b.getTrain().getTrainName() : "")
                .sourceStationCode(b.getSourceStation() != null ? b.getSourceStation().getStationCode() : "")
                .sourceStationName(b.getSourceStation() != null ? b.getSourceStation().getStationName() : "")
                .destinationStationCode(b.getDestinationStation() != null ? b.getDestinationStation().getStationCode() : "")
                .destinationStationName(b.getDestinationStation() != null ? b.getDestinationStation().getStationName() : "")
                .journeyDate(b.getJourneyDate())
                .coachType(b.getCoachType())
                .totalFare(b.getTotalFare())
                .holdToken(b.getHoldToken())
                .passengers(passengerInfos)
                .createdAt(b.getCreatedAt())
                .build();
    }

    private PassengerBookingInfo mapToPassengerInfo(BookingPassenger bp) {
        return PassengerBookingInfo.builder()
                .id(bp.getId())
                .passengerName(bp.getPassenger() != null ? bp.getPassenger().getName() : "")
                .age(bp.getPassenger() != null ? bp.getPassenger().getAge() : 0)
                .gender(bp.getPassenger() != null ? bp.getPassenger().getGender() : null)
                .coachNumber(bp.getCoachNumber())
                .seatNumber(bp.getSeatNumber())
                .berthType(bp.getBerthType())
                .status(bp.getStatus())
                .waitingListNumber(bp.getWaitingListNumber())
                .racNumber(bp.getRacNumber())
                .fare(bp.getFare())
                .build();
    }

    private void publishBookingCreatedEvent(Booking booking, User user) {
        try {
            kafkaTemplate.ifPresent(kt -> {
                BookingCreatedEvent event = BookingCreatedEvent.builder()
                        .bookingId(booking.getId())
                        .pnr(booking.getPnr())
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .trainId(booking.getTrain().getId())
                        .journeyDate(booking.getJourneyDate())
                        .coachType(booking.getCoachType())
                        .totalFare(booking.getTotalFare())
                        .timestamp(Instant.now())
                        .build();
                kt.send("booking-created-events", booking.getPnr(), event);
            });
        } catch (Exception e) {
            log.warn("Could not publish BookingCreatedEvent to Kafka: {}", e.getMessage());
        }
    }

    private void publishBookingConfirmedEvent(Booking booking) {
        try {
            kafkaTemplate.ifPresent(kt -> {
                BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                        .bookingId(booking.getId())
                        .pnr(booking.getPnr())
                        .userId(booking.getUser().getId())
                        .userEmail(booking.getUser().getEmail())
                        .totalFare(booking.getTotalFare())
                        .timestamp(Instant.now())
                        .build();
                kt.send("booking-confirmed-events", booking.getPnr(), event);
            });
        } catch (Exception e) {
            log.warn("Could not publish BookingConfirmedEvent to Kafka: {}", e.getMessage());
        }
    }

    private void publishBookingCancelledEvent(Booking booking) {
        try {
            kafkaTemplate.ifPresent(kt -> {
                BookingCancelledEvent event = BookingCancelledEvent.builder()
                        .bookingId(booking.getId())
                        .pnr(booking.getPnr())
                        .userId(booking.getUser().getId())
                        .refundAmount(booking.getTotalFare())
                        .timestamp(Instant.now())
                        .build();
                kt.send("booking-cancelled-events", booking.getPnr(), event);
            });
        } catch (Exception e) {
            log.warn("Could not publish BookingCancelledEvent to Kafka: {}", e.getMessage());
        }
    }
}
