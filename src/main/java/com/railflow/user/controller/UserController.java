package com.railflow.user.controller;

import com.railflow.common.ApiResponse;
import com.railflow.user.dto.ChangePasswordRequest;
import com.railflow.user.dto.UpdateProfileRequest;
import com.railflow.user.dto.UserProfileDto;
import com.railflow.user.entity.User;
import com.railflow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "Endpoints for managing user profile, account settings, and personal bookings")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user profile", description = "Retrieves the profile information for the authenticated user.")
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@AuthenticationPrincipal User currentUser) {
        UserProfileDto profile = userService.getProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @Operation(summary = "Update user profile", description = "Updates personal details such as name, phone number, and date of birth for the authenticated user.")
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto updatedProfile = userService.updateProfile(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @Operation(summary = "Change password", description = "Changes the account password after validating the current password.")
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @Operation(summary = "Get user bookings", description = "Retrieves all bookings made by the currently authenticated user.")
    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<?>>> getMyBookings(@AuthenticationPrincipal User currentUser) {
        List<?> bookings = userService.getMyBookings(currentUser);
        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", bookings));
    }
}
