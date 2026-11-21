package com.railflow.train.service;

import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.DayOfWeekFlag;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.inventory.dto.AvailabilityResponse;
import com.railflow.inventory.repository.SeatInventoryRepository;
import com.railflow.inventory.service.SeatInventoryService;
import com.railflow.route.dto.RouteDto;
import com.railflow.route.service.RouteService;
import com.railflow.station.dto.StationDto;
import com.railflow.station.entity.Station;
import com.railflow.station.repository.StationRepository;
import com.railflow.train.dto.*;
import com.railflow.train.entity.Coach;
import com.railflow.train.entity.Train;
import com.railflow.train.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainSearchService {

    private final RouteService routeService;
    private final SeatInventoryRepository seatInventoryRepository;
    private final SeatInventoryService seatInventoryService;
    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final TrainService trainService;
    private final CacheManager cacheManager;

    @Cacheable(
            value = "trainSearch",
            key = "#request.sourceStationCode + '-' + #request.destStationCode + '-' + #request.journeyDate + '-' + (#request.coachType != null ? #request.coachType.name() : 'ALL')"
    )
    @Transactional
    public List<TrainSearchResult> searchTrains(TrainSearchRequest request) {
        String srcCode = request.getSourceStationCode().trim().toUpperCase();
        String destCode = request.getDestStationCode().trim().toUpperCase();

        Station srcStation = stationRepository.findByStationCode(srcCode)
                .orElseThrow(() -> new ResourceNotFoundException("Source station not found with code: " + srcCode));
        Station destStation = stationRepository.findByStationCode(destCode)
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found with code: " + destCode));

        // Find train IDs serving both stations in order
        List<Long> routeTrainIds = routeService.findTrainsServingStations(srcCode, destCode);
        Set<Long> trainIds = new LinkedHashSet<>(routeTrainIds);

        // Also check direct source-destination trains if routes not yet configured
        List<Train> directTrains = trainRepository.findBySourceStationAndDestinationStation(srcStation, destStation);
        for (Train dt : directTrains) {
            trainIds.add(dt.getId());
        }

        DayOfWeek dow = request.getJourneyDate().getDayOfWeek();
        DayOfWeekFlag dayFlag = DayOfWeekFlag.valueOf(dow.name());

        List<TrainSearchResult> results = new ArrayList<>();

        for (Long trainId : trainIds) {
            Optional<Train> trainOpt = trainRepository.findById(trainId);
            if (trainOpt.isEmpty()) continue;

            Train train = trainOpt.get();
            if (!train.isActive()) continue;

            // Check running days
            if (train.getRunningDays() != null && !train.getRunningDays().isEmpty()) {
                if (!train.getRunningDays().contains(dayFlag)) {
                    continue;
                }
            }

            // Ensure seat inventory is initialized for this date & segment
            try {
                seatInventoryService.initializeInventory(
                        train.getId(), request.getJourneyDate(), srcStation.getId(), destStation.getId());
            } catch (Exception e) {
                log.warn("Could not auto-initialize inventory for train {}: {}", trainId, e.getMessage());
            }

            // Get coach types for this train
            Set<CoachType> coachTypes = train.getCoaches() != null
                    ? train.getCoaches().stream().map(Coach::getCoachType).collect(Collectors.toSet())
                    : Collections.emptySet();

            if (request.getCoachType() != null && !coachTypes.contains(request.getCoachType())) {
                continue; // Requested class not available on this train
            }

            Map<CoachType, AvailabilityInfo> availabilityMap = new HashMap<>();
            Map<CoachType, BigDecimal> faresMap = new HashMap<>();

            Set<CoachType> typesToQuery = request.getCoachType() != null
                    ? Collections.singleton(request.getCoachType())
                    : coachTypes;

            for (CoachType ct : typesToQuery) {
                long availCount = seatInventoryRepository.countAvailableSeats(
                        train.getId(), request.getJourneyDate(), ct, srcStation.getId(), destStation.getId());
                long totalSeats = seatInventoryRepository.countTotalSeats(
                        train.getId(), request.getJourneyDate(), ct, srcStation.getId(), destStation.getId());

                String status = availCount > 0 ? "AVAILABLE" : (totalSeats > 0 ? "WAITING_LIST" : "NOT_AVAILABLE");

                availabilityMap.put(ct, AvailabilityInfo.builder()
                        .availableSeats((int) availCount)
                        .totalSeats((int) totalSeats)
                        .status(status)
                        .build());

                BigDecimal fare = seatInventoryService.calculateFare(ct, train.getTotalDistance());
                faresMap.put(ct, fare);
            }

            String duration = calculateDuration(train.getDepartureTime(), train.getArrivalTime());

            TrainSearchResult result = TrainSearchResult.builder()
                    .trainId(train.getId())
                    .trainNumber(train.getTrainNumber())
                    .trainName(train.getTrainName())
                    .trainType(train.getTrainType())
                    .sourceStation(mapStation(srcStation))
                    .destStation(mapStation(destStation))
                    .departureTime(train.getDepartureTime())
                    .arrivalTime(train.getArrivalTime())
                    .duration(duration)
                    .journeyDate(request.getJourneyDate())
                    .availability(availabilityMap)
                    .fares(faresMap)
                    .build();

            results.add(result);
        }

        // Sort by departure time
        results.sort(Comparator.comparing(
                TrainSearchResult::getDepartureTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return results;
    }

    @Transactional(readOnly = true)
    public TrainDto getTrainWithFullRoute(Long id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + id));

        TrainDto dto = trainService.mapToDto(train, true);
        try {
            RouteDto route = routeService.getRouteForTrain(id);
            dto.setRoute(route);
        } catch (Exception e) {
            log.debug("No route configured for train id: {}", id);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getTrainAvailability(Long trainId, LocalDate date, CoachType coachType) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + trainId));

        Long srcId = train.getSourceStation() != null ? train.getSourceStation().getId() : null;
        Long destId = train.getDestinationStation() != null ? train.getDestinationStation().getId() : null;

        return seatInventoryService.getAvailability(trainId, date, coachType, srcId, destId);
    }

    private String calculateDuration(LocalTime departure, LocalTime arrival) {
        if (departure == null || arrival == null) return "N/A";
        long minutes;
        if (!arrival.isBefore(departure)) {
            minutes = Duration.between(departure, arrival).toMinutes();
        } else {
            minutes = Duration.between(departure, LocalTime.MAX).toMinutes() + 1
                    + Duration.between(LocalTime.MIN, arrival).toMinutes();
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return String.format("%dh %02dm", hours, remainingMinutes);
    }

    private StationDto mapStation(Station station) {
        if (station == null) return null;
        return StationDto.builder()
                .id(station.getId())
                .stationCode(station.getStationCode())
                .stationName(station.getStationName())
                .city(station.getCity())
                .state(station.getState())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .zoneCode(station.getZoneCode())
                .isActive(station.isActive())
                .createdAt(station.getCreatedAt())
                .updatedAt(station.getUpdatedAt())
                .build();
    }
}
