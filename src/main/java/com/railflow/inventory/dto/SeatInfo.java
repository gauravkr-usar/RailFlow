package com.railflow.inventory.dto;

import com.railflow.common.enums.BerthType;
import com.railflow.common.enums.CoachType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatInfo {
    private Long seatId;
    private Long inventoryId;
    private String coachNumber;
    private Integer seatNumber;
    private BerthType berthType;
    private CoachType coachType;
    private BigDecimal fare;
}
