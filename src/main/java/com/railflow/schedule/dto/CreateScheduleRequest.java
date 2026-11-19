package com.railflow.schedule.dto;

import com.railflow.common.enums.ScheduleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {

    @NotNull(message = "Train ID is required")
    private Long trainId;

    @NotNull(message = "Journey date is required")
    private LocalDate journeyDate;

    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.ON_TIME;

    @Builder.Default
    private Integer delayMinutes = 0;

    private String remarks;
}
