package com.railflow.booking.dto;

import com.railflow.common.enums.CoachType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBookingRequest {

    @NotNull(message = "Train ID is required")
    private Long trainId;

    @NotBlank(message = "Source station code is required")
    private String sourceStationCode;

    @NotBlank(message = "Destination station code is required")
    private String destinationStationCode;

    @NotNull(message = "Journey date is required")
    @FutureOrPresent(message = "Journey date must be today or in the future")
    private LocalDate journeyDate;

    @NotNull(message = "Coach type is required")
    private CoachType coachType;

    @NotEmpty(message = "At least one passenger is required")
    @Size(max = 6, message = "Maximum 6 passengers allowed per booking")
    @Valid
    private List<PassengerRequest> passengers;

    private String holdToken;

    private String idempotencyKey;
}
