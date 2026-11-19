package com.railflow.booking.dto;

import com.railflow.common.enums.BookingStatus;
import com.railflow.common.enums.CoachType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String pnr;
    private BookingStatus status;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStationCode;
    private String sourceStationName;
    private String destinationStationCode;
    private String destinationStationName;
    private LocalDate journeyDate;
    private CoachType coachType;
    private BigDecimal totalFare;
    private String holdToken;
    private List<PassengerBookingInfo> passengers;
    private LocalDateTime createdAt;
}
