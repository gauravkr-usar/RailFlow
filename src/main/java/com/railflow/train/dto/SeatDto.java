package com.railflow.train.dto;

import com.railflow.common.enums.BerthType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {

    private Long id;
    private Integer seatNumber;
    private BerthType berthType;
    private Long coachId;
    private boolean isActive;
}
