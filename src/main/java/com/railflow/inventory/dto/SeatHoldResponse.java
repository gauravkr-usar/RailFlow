package com.railflow.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldResponse {
    private String holdToken;
    private List<SeatInfo> seats;
    private LocalDateTime expiresAt;
    private BigDecimal totalFare;
}
