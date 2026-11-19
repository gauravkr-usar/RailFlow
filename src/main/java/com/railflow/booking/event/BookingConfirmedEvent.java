package com.railflow.booking.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record BookingConfirmedEvent(
        Long bookingId,
        String pnr,
        Long userId,
        String userEmail,
        BigDecimal totalFare,
        Instant timestamp
) {}
