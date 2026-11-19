package com.railflow.inventory.dto;

import com.railflow.common.enums.BerthType;
import com.railflow.common.enums.CoachType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsRequest {

    @NotNull(message = "trainId is required")
    private Long trainId;

    @NotNull(message = "journeyDate is required")
    @FutureOrPresent(message = "journeyDate cannot be in the past")
    private LocalDate journeyDate;

    @NotNull(message = "coachType is required")
    private CoachType coachType;

    @NotNull(message = "passengerCount is required")
    @Min(value = 1, message = "passengerCount must be at least 1")
    @Max(value = 6, message = "passengerCount cannot exceed 6 per booking")
    private Integer passengerCount;

    private Long userId;

    @NotNull(message = "sourceStationId is required")
    private Long sourceStationId;

    @NotNull(message = "destStationId is required")
    private Long destStationId;

    private List<BerthType> berthPreferences;
}
