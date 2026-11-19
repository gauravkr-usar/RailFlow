package com.railflow.cancellation.dto;

import com.railflow.common.enums.RefundStatus;
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
public class CancellationResponse {

    private Long bookingId;
    private String pnr;
    private BigDecimal originalFare;
    private BigDecimal cancellationCharges;
    private BigDecimal refundAmount;
    private RefundStatus refundStatus;
    private LocalDateTime cancellationTime;
}
