package com.railflow.payment.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSuccessfulEvent(
        Long bookingId,
        String pnr,
        Long userId,
        String userEmail,
        BigDecimal amount,
        String razorpayPaymentId,
        Instant timestamp
) {
}
