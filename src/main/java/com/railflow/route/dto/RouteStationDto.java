package com.railflow.route.dto;

import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStationDto {

    private Long id;
    private Long stationId;
    private String stationCode;
    private String stationName;
    private String city;
    private Integer sequenceNumber;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private Integer distanceFromSource;
    private Integer dayOffset;
    private String platformNumber;
}
