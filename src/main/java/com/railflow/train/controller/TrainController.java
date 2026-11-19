package com.railflow.train.controller;

import com.railflow.train.dto.*;
import com.railflow.train.service.TrainService;
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
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @GetMapping
    public ResponseEntity<Page<TrainDto>> getAllTrains(
            @PageableDefault(size = 20, sort = "trainNumber") Pageable pageable) {
        return ResponseEntity.ok(trainService.getAllTrains(pageable));
    }

    @GetMapping("/number/{trainNumber}")
    public ResponseEntity<TrainDto> getTrainByNumber(@PathVariable String trainNumber) {
        return ResponseEntity.ok(trainService.getTrainByNumber(trainNumber));
    }

    @GetMapping("/between")
    public ResponseEntity<List<TrainDto>> getTrainsBetween(
            @RequestParam("sourceId") Long sourceId,
            @RequestParam("destId") Long destId) {
        return ResponseEntity.ok(trainService.getTrainsBetweenStations(sourceId, destId));
    }

    @GetMapping("/{id}/coaches")
    public ResponseEntity<List<CoachDto>> getCoachesForTrain(@PathVariable Long id) {
        return ResponseEntity.ok(trainService.getCoachesForTrain(id));
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<TrainDto> getTrainWithCoaches(@PathVariable Long id) {
        return ResponseEntity.ok(trainService.getTrainWithCoaches(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainDto> createTrain(@Valid @RequestBody CreateTrainRequest request) {
        TrainDto created = trainService.createTrain(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainDto> updateTrain(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainRequest request) {
        return ResponseEntity.ok(trainService.updateTrain(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrain(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/coaches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachDto> addCoach(
            @PathVariable Long id,
            @Valid @RequestBody AddCoachRequest request) {
        CoachDto coach = trainService.addCoach(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(coach);
    }

    @PostMapping("/coaches/{coachId}/seats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SeatDto>> addSeatsToCoach(
            @PathVariable Long coachId,
            @Valid @RequestBody List<AddSeatRequest> requests) {
        List<SeatDto> seats = trainService.addSeatsToCoach(coachId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(seats);
    }
}
