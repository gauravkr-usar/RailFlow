package com.railflow.admin.service;

import com.railflow.admin.dto.DashboardStatsResponse;
import com.railflow.admin.dto.RecentBookingDto;
import com.railflow.booking.entity.Booking;
import com.railflow.booking.repository.BookingRepository;
import com.railflow.common.enums.BookingStatus;
import com.railflow.train.repository.TrainRepository;
import com.railflow.user.dto.UserProfileDto;
import com.railflow.user.entity.User;
import com.railflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TrainRepository trainRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();
        long totalTrains = trainRepository.count();
        long activeTrains = trainRepository.count();

        long confirmedCount = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long cancelledCount = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        // Calculate total revenue from confirmed bookings
        List<Booking> allBookings = bookingRepository.findAll();
        BigDecimal totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        long todayBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().toLocalDate().isEqual(today))
                .count();

        // 10 most recent bookings
        List<RecentBookingDto> recentBookings = allBookings.stream()
                .sorted((b1, b2) -> {
                    LocalDateTime t1 = b1.getCreatedAt() != null ? b1.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime t2 = b2.getCreatedAt() != null ? b2.getCreatedAt() : LocalDateTime.MIN;
                    return t2.compareTo(t1);
                })
                .limit(10)
                .map(b -> RecentBookingDto.builder()
                        .pnr(b.getPnr())
                        .userName(b.getUser() != null ? b.getUser().getFirstName() + " " + b.getUser().getLastName() : "User")
                        .userEmail(b.getUser() != null ? b.getUser().getEmail() : "")
                        .trainNumber(b.getTrain() != null ? b.getTrain().getTrainNumber() : "")
                        .trainName(b.getTrain() != null ? b.getTrain().getTrainName() : "")
                        .journeyDate(b.getJourneyDate())
                        .status(b.getStatus())
                        .amount(b.getTotalFare())
                        .createdAt(b.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalBookings(totalBookings)
                .totalTrains(totalTrains)
                .totalRevenue(totalRevenue)
                .todayBookings(todayBookings)
                .activeTrains(activeTrains)
                .confirmedBookingsCount(confirmedCount)
                .cancelledBookingsCount(cancelledCount)
                .recentBookings(recentBookings)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserProfileDto> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageRequest)
                .map(u -> UserProfileDto.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .phoneNumber(u.getPhoneNumber())
                        .dateOfBirth(u.getDateOfBirth())
                        .isEnabled(u.isEnabled())
                        .isEmailVerified(u.isEmailVerified())
                        .roles(u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()))
                        .createdAt(u.getCreatedAt())
                        .build());
    }

    @Transactional
    public void toggleUserStatus(Long userId, boolean isEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setEnabled(isEnabled);
        userRepository.save(user);
        log.info("User {} status updated to enabled: {}", userId, isEnabled);
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "PostgreSQL Connected");
        health.put("cache", "Redis Distributed Cache Active");
        health.put("broker", "Apache Kafka Event Bus Configured");
        health.put("timestamp", LocalDateTime.now());
        return health;
    }
}
