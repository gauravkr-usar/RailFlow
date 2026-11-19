package com.railflow.route.controller;

import com.railflow.route.dto.CreateRouteRequest;
import com.railflow.route.dto.CreateRouteStationRequest;
import com.railflow.route.dto.RouteDto;
import com.railflow.route.dto.RouteStationDto;
import com.railflow.route.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/trains/{trainId}/route")
    public ResponseEntity<RouteDto> getRouteForTrain(@PathVariable Long trainId) {
        return ResponseEntity.ok(routeService.getRouteForTrain(trainId));
    }

    @PostMapping("/trains/{trainId}/route")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteDto> createRoute(
            @PathVariable Long trainId,
            @Valid @RequestBody CreateRouteRequest request) {
        RouteDto route = routeService.createRoute(trainId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(route);
    }

    @PostMapping("/trains/{trainId}/stations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteStationDto> addStationToRoute(
            @PathVariable Long trainId,
            @Valid @RequestBody CreateRouteStationRequest request) {
        RouteStationDto station = routeService.addStationToRoute(trainId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(station);
    }

    @PutMapping("/stations/{routeStationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteStationDto> updateRouteStation(
            @PathVariable Long routeStationId,
            @Valid @RequestBody CreateRouteStationRequest request) {
        return ResponseEntity.ok(routeService.updateRouteStation(routeStationId, request));
    }

    @GetMapping("/serving")
    public ResponseEntity<List<Long>> findTrainsServingStations(
            @RequestParam("from") String sourceCode,
            @RequestParam("to") String destCode) {
        return ResponseEntity.ok(routeService.findTrainsServingStations(sourceCode, destCode));
    }

    @GetMapping("/trains/{trainId}/stations-between")
    public ResponseEntity<List<RouteStationDto>> getStationsBetween(
            @PathVariable Long trainId,
            @RequestParam("from") String sourceCode,
            @RequestParam("to") String destCode) {
        return ResponseEntity.ok(routeService.getRouteStationsBetween(trainId, sourceCode, destCode));
    }
}
