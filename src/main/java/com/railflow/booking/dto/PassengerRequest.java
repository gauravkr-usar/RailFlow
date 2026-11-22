package com.railflow.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.railflow.common.enums.BerthType;
import com.railflow.common.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassengerRequest {

    @NotBlank(message = "Passenger name is required")
    private String name;

    @NotNull(message = "Passenger age is required")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private BerthType berthPreference;

    private String idProofType;

    private String idProofNumber;

    private Integer chosenSeatNumber;

    private String chosenCoach;

    private String chosenBerthType;
}
