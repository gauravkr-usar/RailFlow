package com.railflow.inventory.dto;

import com.railflow.common.enums.CoachType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachAvailability {
    private String coachNumber;
    private CoachType coachType;
    private int availableSeats;
    private int totalSeats;
}
