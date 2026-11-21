package com.railflow.admin.controller;

import com.railflow.admin.dto.DashboardStatsResponse;
import com.railflow.admin.service.AdminDashboardService;
import com.railflow.common.ApiResponse;
import com.railflow.user.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administration and Operations Dashboard APIs")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get overall railway system statistics and metrics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", stats));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get paginated user accounts")
    public ResponseEntity<ApiResponse<Page<UserProfileDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserProfileDto> users = adminDashboardService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable or disable user account")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        adminDashboardService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success("User status updated", null));
    }

    @GetMapping("/health-summary")
    @Operation(summary = "Get high-level subsystem health summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealthSummary() {
        Map<String, Object> health = adminDashboardService.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.success("System health status", health));
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all system bookings for admin audit")
    public ResponseEntity<ApiResponse<java.util.List<com.railflow.admin.dto.RecentBookingDto>>> getAllBookings() {
        java.util.List<com.railflow.admin.dto.RecentBookingDto> bookings = adminDashboardService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success("All system bookings fetched", bookings));
    }
}
