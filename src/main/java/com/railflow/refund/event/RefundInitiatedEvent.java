package com.railflow.refund.event;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundInitiatedEvent(
        Long bookingId,
        String pnr,
        Long userId,
        BigDecimal refundAmount,
        Instant timestamp
) {
}
