package com.railflow.inventory.controller;

import com.railflow.common.enums.CoachType;
import com.railflow.inventory.dto.AvailabilityResponse;
import com.railflow.inventory.dto.HoldSeatsRequest;
import com.railflow.inventory.dto.SeatHoldResponse;
import com.railflow.inventory.service.SeatInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final SeatInventoryService seatInventoryService;

    @GetMapping
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @RequestParam("trainId") Long trainId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("coachType") CoachType coachType,
            @RequestParam(value = "srcStationId", required = false) Long srcStationId,
            @RequestParam(value = "destStationId", required = false) Long destStationId) {
        AvailabilityResponse response = seatInventoryService.getAvailability(
                trainId, date, coachType, srcStationId, destStationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hold")
    public ResponseEntity<SeatHoldResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request) {
        SeatHoldResponse response = seatInventoryService.holdSeats(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/hold/{holdToken}")
    public ResponseEntity<Void> releaseHold(@PathVariable String holdToken) {
        seatInventoryService.releaseHold(holdToken);
        return ResponseEntity.noContent().build();
    }
}
