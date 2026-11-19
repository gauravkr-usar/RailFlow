package com.railflow.train.dto;

import com.railflow.common.enums.CoachType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCoachRequest {

    @NotBlank(message = "Coach number is required (e.g. S1, B1, A1)")
    private String coachNumber;

    @NotNull(message = "Coach type is required")
    private CoachType coachType;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be greater than 0")
    private Integer totalSeats;
}
