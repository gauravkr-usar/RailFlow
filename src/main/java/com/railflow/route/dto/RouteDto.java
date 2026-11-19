package com.railflow.route.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {

    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private List<RouteStationDto> routeStations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
