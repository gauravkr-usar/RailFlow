package com.railflow.payment.dto;

import com.railflow.common.enums.PaymentMethod;
import com.railflow.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {

    private Long paymentId;
    private Long bookingId;
    private PaymentStatus status;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
}
