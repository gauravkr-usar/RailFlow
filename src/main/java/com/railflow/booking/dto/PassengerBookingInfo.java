package com.railflow.booking.dto;

import com.railflow.common.enums.BerthType;
import com.railflow.common.enums.Gender;
import com.railflow.common.enums.PassengerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerBookingInfo {
    private Long id;
    private String passengerName;
    private Integer age;
    private Gender gender;
    private String coachNumber;
    private Integer seatNumber;
    private BerthType berthType;
    private PassengerStatus status;
    private Integer waitingListNumber;
    private Integer racNumber;
    private BigDecimal fare;
}
