package com.railflow.cancellation.controller;

import com.railflow.cancellation.dto.CancellationResponse;
import com.railflow.cancellation.service.CancellationService;
import com.railflow.common.ApiResponse;
import com.railflow.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cancellations")
@RequiredArgsConstructor
@Tag(name = "Cancellation", description = "Ticket Cancellation and Charges Calculation APIs")
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping("/booking/{bookingId}")
    @Operation(summary = "Cancel booking and initiate refund")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        CancellationResponse response = cancellationService.cancelBooking(bookingId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket cancelled successfully", response));
    }
}
