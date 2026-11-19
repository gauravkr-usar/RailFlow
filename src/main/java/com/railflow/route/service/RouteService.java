package com.railflow.route.service;

import com.railflow.route.dto.CreateRouteRequest;
import com.railflow.route.dto.CreateRouteStationRequest;
import com.railflow.route.dto.RouteDto;
import com.railflow.route.dto.RouteStationDto;
import com.railflow.route.entity.RouteStation;
import com.railflow.route.entity.TrainRoute;
import com.railflow.route.repository.RouteStationRepository;
import com.railflow.route.repository.TrainRouteRepository;
import com.railflow.station.entity.Station;
import com.railflow.station.repository.StationRepository;
import com.railflow.train.entity.Train;
import com.railflow.train.repository.TrainRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RouteService {

    private final TrainRouteRepository trainRouteRepository;
    private final RouteStationRepository routeStationRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;

    public RouteDto createRoute(Long trainId, CreateRouteRequest request) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + trainId));

        TrainRoute trainRoute = trainRouteRepository.findByTrainId(trainId)
                .orElse(TrainRoute.builder().train(train).routeStations(new ArrayList<>()).build());

        trainRoute.getRouteStations().clear();

        List<RouteStation> routeStations = new ArrayList<>();
        for (CreateRouteStationRequest req : request.getStations()) {
            Station station = stationRepository.findById(req.getStationId())
                    .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + req.getStationId()));

            RouteStation rs = RouteStation.builder()
                    .trainRoute(trainRoute)
                    .station(station)
                    .sequenceNumber(req.getSequenceNumber())
                    .arrivalTime(req.getArrivalTime())
                    .departureTime(req.getDepartureTime())
                    .distanceFromSource(req.getDistanceFromSource())
                    .dayOffset(req.getDayOffset() != null ? req.getDayOffset() : 0)
                    .platformNumber(req.getPlatformNumber())
                    .build();
            routeStations.add(rs);
        }

        trainRoute.getRouteStations().addAll(routeStations);
        TrainRoute saved = trainRouteRepository.save(trainRoute);
        log.info("Saved route for train id {} with {} stations", trainId, saved.getRouteStations().size());
        return mapToDto(saved);
    }

    public RouteStationDto addStationToRoute(Long trainId, CreateRouteStationRequest request) {
        TrainRoute trainRoute = trainRouteRepository.findByTrainId(trainId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found for train id: " + trainId));

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + request.getStationId()));

        RouteStation rs = RouteStation.builder()
                .trainRoute(trainRoute)
                .station(station)
                .sequenceNumber(request.getSequenceNumber())
                .arrivalTime(request.getArrivalTime())
                .departureTime(request.getDepartureTime())
                .distanceFromSource(request.getDistanceFromSource())
                .dayOffset(request.getDayOffset() != null ? request.getDayOffset() : 0)
                .platformNumber(request.getPlatformNumber())
                .build();

        RouteStation saved = routeStationRepository.save(rs);
        log.info("Added station {} to route for train id {}", station.getStationCode(), trainId);
        return mapStationToDto(saved);
    }

    public RouteStationDto updateRouteStation(Long routeStationId, CreateRouteStationRequest request) {
        RouteStation rs = routeStationRepository.findById(routeStationId)
                .orElseThrow(() -> new EntityNotFoundException("RouteStation not found with id: " + routeStationId));

        if (request.getStationId() != null && !rs.getStation().getId().equals(request.getStationId())) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + request.getStationId()));
            rs.setStation(station);
        }

        rs.setSequenceNumber(request.getSequenceNumber());
        rs.setArrivalTime(request.getArrivalTime());
        rs.setDepartureTime(request.getDepartureTime());
        rs.setDistanceFromSource(request.getDistanceFromSource());
        if (request.getDayOffset() != null) {
            rs.setDayOffset(request.getDayOffset());
        }
        rs.setPlatformNumber(request.getPlatformNumber());

        RouteStation saved = routeStationRepository.save(rs);
        return mapStationToDto(saved);
    }

    @Transactional(readOnly = true)
    public RouteDto getRouteForTrain(Long trainId) {
        TrainRoute trainRoute = trainRouteRepository.findByTrainId(trainId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found for train id: " + trainId));
        return mapToDto(trainRoute);
    }

    @Transactional(readOnly = true)
    public List<Long> findTrainsServingStations(String sourceCode, String destCode) {
        return routeStationRepository.findTrainIdsServingStations(sourceCode.toUpperCase(), destCode.toUpperCase());
    }

    @Transactional(readOnly = true)
    public List<RouteStationDto> getRouteStationsBetween(Long trainId, String sourceCode, String destCode) {
        return trainRouteRepository.findRouteStationsBetween(trainId, sourceCode.toUpperCase(), destCode.toUpperCase())
                .stream()
                .map(this::mapStationToDto)
                .collect(Collectors.toList());
    }

    public RouteDto mapToDto(TrainRoute trainRoute) {
        List<RouteStationDto> stationDtos = trainRoute.getRouteStations() != null
                ? trainRoute.getRouteStations().stream()
                    .sorted(Comparator.comparing(RouteStation::getSequenceNumber))
                    .map(this::mapStationToDto)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        Train train = trainRoute.getTrain();
        return RouteDto.builder()
                .id(trainRoute.getId())
                .trainId(train != null ? train.getId() : null)
                .trainNumber(train != null ? train.getTrainNumber() : null)
                .trainName(train != null ? train.getTrainName() : null)
                .routeStations(stationDtos)
                .createdAt(trainRoute.getCreatedAt())
                .updatedAt(trainRoute.getUpdatedAt())
                .build();
    }

    public RouteStationDto mapStationToDto(RouteStation rs) {
        RouteStationDto.RouteStationDtoBuilder builder = RouteStationDto.builder()
                .id(rs.getId())
                .sequenceNumber(rs.getSequenceNumber())
                .arrivalTime(rs.getArrivalTime())
                .departureTime(rs.getDepartureTime())
                .distanceFromSource(rs.getDistanceFromSource())
                .dayOffset(rs.getDayOffset())
                .platformNumber(rs.getPlatformNumber());

        if (rs.getStation() != null) {
            builder.stationId(rs.getStation().getId())
                   .stationCode(rs.getStation().getStationCode())
                   .stationName(rs.getStation().getStationName())
                   .city(rs.getStation().getCity());
        }

        return builder.build();
    }
}
