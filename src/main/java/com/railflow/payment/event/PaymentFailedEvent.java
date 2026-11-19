package com.railflow.payment.event;

import java.time.Instant;

public record PaymentFailedEvent(
        Long bookingId,
        String pnr,
        Long userId,
        String reason,
        Instant timestamp
) {
}
