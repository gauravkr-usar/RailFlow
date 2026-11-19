package com.railflow.train.dto;

import com.railflow.common.enums.BerthType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSeatRequest {

    @NotNull(message = "Seat number is required")
    @Positive(message = "Seat number must be greater than 0")
    private Integer seatNumber;

    @NotNull(message = "BerthType is required")
    private BerthType berthType;
}
