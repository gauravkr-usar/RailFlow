package com.railflow.station.controller;

import com.railflow.station.dto.CreateStationRequest;
import com.railflow.station.dto.StationDto;
import com.railflow.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<Page<StationDto>> getAllStations(
            @PageableDefault(size = 20, sort = "stationName") Pageable pageable) {
        return ResponseEntity.ok(stationService.getAllStations(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StationDto>> searchStations(
            @RequestParam("q") String query,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(stationService.searchStations(query, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StationDto>> getActiveStations() {
        return ResponseEntity.ok(stationService.getAllActiveStations());
    }

    @GetMapping("/{code}")
    public ResponseEntity<StationDto> getStationByCode(@PathVariable String code) {
        return ResponseEntity.ok(stationService.getStationByCode(code));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StationDto> createStation(@Valid @RequestBody CreateStationRequest request) {
        StationDto created = stationService.createStation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StationDto> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody CreateStationRequest request) {
        return ResponseEntity.ok(stationService.updateStation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}
