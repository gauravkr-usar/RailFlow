package com.railflow.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalBookings;
    private long totalTrains;
    private BigDecimal totalRevenue;
    private long todayBookings;
    private long activeTrains;
    private long confirmedBookingsCount;
    private long cancelledBookingsCount;
    private List<RecentBookingDto> recentBookings;
}
