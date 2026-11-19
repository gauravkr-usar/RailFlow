package com.railflow.station.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDto {

    private Long id;
    private String stationCode;
    private String stationName;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
    private String zoneCode;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
