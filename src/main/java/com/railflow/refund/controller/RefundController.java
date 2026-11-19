package com.railflow.refund.controller;

import com.railflow.common.ApiResponse;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.refund.entity.Refund;
import com.railflow.refund.repository.RefundRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "Refund Status and Tracking APIs")
public class RefundController {

    private final RefundRepository refundRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Get refund by ID")
    public ResponseEntity<ApiResponse<Refund>> getRefundById(@PathVariable Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success("Refund found", refund));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get refunds for a booking")
    public ResponseEntity<ApiResponse<List<Refund>>> getRefundsByBooking(@PathVariable Long bookingId) {
        List<Refund> refunds = refundRepository.findByBooking_Id(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Refunds fetched", refunds));
    }
}
