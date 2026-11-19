package com.railflow.train.dto;

import com.railflow.common.enums.DayOfWeekFlag;
import com.railflow.common.enums.TrainType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainRequest {

    @NotBlank(message = "Train number is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "Train number must be a 5-digit number")
    private String trainNumber;

    @NotBlank(message = "Train name is required")
    @Size(max = 200, message = "Train name must not exceed 200 characters")
    private String trainName;

    @NotNull(message = "Train type is required")
    private TrainType trainType;

    @NotNull(message = "Source station ID is required")
    private Long sourceStationId;

    @NotNull(message = "Destination station ID is required")
    private Long destinationStationId;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;

    @NotEmpty(message = "At least one running day is required")
    private Set<DayOfWeekFlag> runningDays;

    @Min(value = 1, message = "Total distance must be at least 1 km")
    private Integer totalDistance;
}
