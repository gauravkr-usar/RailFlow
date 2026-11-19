package com.railflow.station.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStationRequest {

    @NotBlank(message = "Station code is required")
    @Size(min = 2, max = 10, message = "Station code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Station code must contain only uppercase letters and digits")
    private String stationCode;

    @NotBlank(message = "Station name is required")
    @Size(max = 200, message = "Station name must not exceed 200 characters")
    private String stationName;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Size(max = 10, message = "Zone code must not exceed 10 characters")
    private String zoneCode;
}
