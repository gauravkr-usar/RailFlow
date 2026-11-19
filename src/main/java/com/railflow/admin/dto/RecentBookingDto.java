package com.railflow.admin.dto;

import com.railflow.common.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentBookingDto {
    private String pnr;
    private String userName;
    private String userEmail;
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private BookingStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
