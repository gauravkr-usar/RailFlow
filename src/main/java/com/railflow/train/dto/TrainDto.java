package com.railflow.train.dto;

import com.railflow.common.enums.DayOfWeekFlag;
import com.railflow.common.enums.TrainType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainDto {

    private Long id;
    private String trainNumber;
    private String trainName;
    private TrainType trainType;
    private Long sourceStationId;
    private String sourceStationCode;
    private String sourceStationName;
    private Long destinationStationId;
    private String destinationStationCode;
    private String destinationStationName;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Set<DayOfWeekFlag> runningDays;
    private Integer totalDistance;
    private boolean isActive;
    private List<CoachDto> coaches;
    private com.railflow.route.dto.RouteDto route;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
