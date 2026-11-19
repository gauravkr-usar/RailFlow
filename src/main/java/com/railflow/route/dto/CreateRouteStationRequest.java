package com.railflow.route.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteStationRequest {

    @NotNull(message = "Station ID is required")
    private Long stationId;

    @NotNull(message = "Sequence number is required")
    @PositiveOrZero(message = "Sequence number must be non-negative")
    private Integer sequenceNumber;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    @PositiveOrZero(message = "Distance from source must be non-negative")
    private Integer distanceFromSource;

    @Builder.Default
    private Integer dayOffset = 0;

    private String platformNumber;
}
