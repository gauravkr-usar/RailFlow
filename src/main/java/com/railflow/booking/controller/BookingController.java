package com.railflow.booking.controller;

import com.railflow.booking.dto.BookingResponse;
import com.railflow.booking.dto.CreateBookingRequest;
import com.railflow.booking.dto.PnrStatusResponse;
import com.railflow.booking.service.BookingService;
import com.railflow.common.ApiResponse;
import com.railflow.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking", description = "Railway Ticket Reservation and PNR APIs")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create ticket booking (supports Idempotency-Key header)")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            @AuthenticationPrincipal User currentUser) {

        if (idempotencyHeader != null && !idempotencyHeader.isBlank() && request.getIdempotencyKey() == null) {
            request.setIdempotencyKey(idempotencyHeader);
        }

        BookingResponse response = bookingService.createBooking(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking initiated successfully. Please complete payment.", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's booking history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal User currentUser) {
        List<BookingResponse> bookings = bookingService.getUserBookings(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Fetched user bookings", bookings));
    }

    @GetMapping("/{pnr}")
    @Operation(summary = "Get PNR enquiry details")
    public ResponseEntity<ApiResponse<PnrStatusResponse>> getBookingByPnr(@PathVariable String pnr) {
        PnrStatusResponse response = bookingService.getBookingByPnr(pnr);
        return ResponseEntity.ok(ApiResponse.success("PNR details fetched", response));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking fetched", response));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm booking (internal / test)")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking by ID")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        bookingService.cancelBooking(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}
