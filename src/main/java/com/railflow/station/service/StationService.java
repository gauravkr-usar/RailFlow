package com.railflow.station.service;

import com.railflow.common.mapper.StationMapper;
import com.railflow.station.dto.CreateStationRequest;
import com.railflow.station.dto.StationDto;
import com.railflow.station.entity.Station;
import com.railflow.station.repository.StationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StationService {

    private final StationRepository stationRepository;
    private final StationMapper stationMapper;

    public StationDto createStation(CreateStationRequest request) {
        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new IllegalArgumentException("Station with code '" + request.getStationCode() + "' already exists");
        }
        Station station = Station.builder()
                .stationCode(request.getStationCode().toUpperCase())
                .stationName(request.getStationName())
                .city(request.getCity())
                .state(request.getState())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .zoneCode(request.getZoneCode())
                .isActive(true)
                .build();
        Station saved = stationRepository.save(station);
        log.info("Created station: {} - {}", saved.getStationCode(), saved.getStationName());
        return stationMapper.toDto(saved);
    }

    public StationDto updateStation(Long id, CreateStationRequest request) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
        // If code is changing, check uniqueness
        if (!station.getStationCode().equals(request.getStationCode()) &&
                stationRepository.existsByStationCode(request.getStationCode())) {
            throw new IllegalArgumentException("Station with code '" + request.getStationCode() + "' already exists");
        }
        station.setStationCode(request.getStationCode().toUpperCase());
        station.setStationName(request.getStationName());
        station.setCity(request.getCity());
        station.setState(request.getState());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setZoneCode(request.getZoneCode());
        Station saved = stationRepository.save(station);
        log.info("Updated station: {} - {}", saved.getStationCode(), saved.getStationName());
        return stationMapper.toDto(saved);
    }

    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
        station.setActive(false);
        stationRepository.save(station);
        log.info("Soft-deleted station id: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<StationDto> getAllStations(Pageable pageable) {
        return stationRepository.findAll(pageable).map(stationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StationDto> searchStations(String query, Pageable pageable) {
        return stationRepository.searchByNameOrCode(query, pageable).map(stationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public StationDto getStationByCode(String code) {
        Station station = stationRepository.findByStationCode(code.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Station not found with code: " + code));
        return stationMapper.toDto(station);
    }

    @Transactional(readOnly = true)
    public StationDto getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
        return stationMapper.toDto(station);
    }

    @Transactional(readOnly = true)
    public List<StationDto> getAllActiveStations() {
        return stationRepository.findByIsActiveTrue()
                .stream()
                .map(stationMapper::toDto)
                .collect(Collectors.toList());
    }
}
