package com.railflow.schedule.dto;

import com.railflow.common.enums.ScheduleStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {

    private ScheduleStatus status;
    private Integer delayMinutes;
    private String remarks;
}
