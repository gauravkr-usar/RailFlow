package com.railflow.passenger.controller;

import com.railflow.booking.dto.PassengerRequest;
import com.railflow.passenger.dto.PassengerResponse;
import com.railflow.passenger.service.PassengerService;
import com.railflow.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
@Tag(name = "Passenger Management", description = "Endpoints for managing saved passenger profiles")
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add passenger profile", description = "Saves a new passenger profile for the authenticated user")
    public ResponseEntity<PassengerResponse> addPassengerProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PassengerRequest request) {
        return new ResponseEntity<>(passengerService.addPassengerProfile(currentUser, request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's passengers", description = "Fetches all saved active passenger profiles of the authenticated user")
    public ResponseEntity<List<PassengerResponse>> getMyPassengers(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(passengerService.getMyPassengers(currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete passenger profile", description = "Soft deletes a passenger profile belonging to the authenticated user")
    public ResponseEntity<Void> deletePassengerProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        passengerService.deletePassengerProfile(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
