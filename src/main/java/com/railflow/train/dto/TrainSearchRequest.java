package com.railflow.train.dto;

import com.railflow.common.enums.CoachType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainSearchRequest {

    @NotBlank(message = "sourceStationCode is required")
    private String sourceStationCode;

    @NotBlank(message = "destStationCode is required")
    private String destStationCode;

    @NotNull(message = "journeyDate is required")
    @FutureOrPresent(message = "journeyDate cannot be in the past")
    private LocalDate journeyDate;

    private CoachType coachType;

    @Builder.Default
    private Integer passengerCount = 1;
}
