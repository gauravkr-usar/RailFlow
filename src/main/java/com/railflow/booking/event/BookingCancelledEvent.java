package com.railflow.booking.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record BookingCancelledEvent(
        Long bookingId,
        String pnr,
        Long userId,
        BigDecimal refundAmount,
        Instant timestamp
) {}
