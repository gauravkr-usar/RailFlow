package com.railflow.schedule.dto;

import com.railflow.common.enums.ScheduleStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainScheduleDto {

    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private ScheduleStatus status;
    private Integer delayMinutes;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
