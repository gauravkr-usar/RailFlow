package com.railflow.inventory.dto;

import com.railflow.common.enums.CoachType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private Long trainId;
    private LocalDate journeyDate;
    private CoachType coachType;
    private long availableCount;
    private BigDecimal fare;
    private List<CoachAvailability> coaches;
}
