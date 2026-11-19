package com.railflow.train.dto;

import com.railflow.common.enums.CoachType;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachDto {

    private Long id;
    private String coachNumber;
    private CoachType coachType;
    private Long trainId;
    private Integer totalSeats;
    private boolean isActive;
    private List<SeatDto> seats;
}
