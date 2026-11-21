package com.railflow.station.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDto implements Serializable {
    private static final long serialVersionUID = 1L;

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
