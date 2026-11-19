package com.railflow.booking.event;

import com.railflow.common.enums.CoachType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Builder
public record BookingCreatedEvent(
        Long bookingId,
        String pnr,
        Long userId,
        String userEmail,
        Long trainId,
        LocalDate journeyDate,
        CoachType coachType,
        BigDecimal totalFare,
        Instant timestamp
) {}
