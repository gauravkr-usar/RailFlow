package com.railflow.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railflow.auth.dto.*;
import com.railflow.auth.service.AuthService;
import com.railflow.auth.service.IdempotencyService;
import com.railflow.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token refresh, and password recovery")
public class AuthController {

    private final AuthService authService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Register new user", description = "Registers a new user account with role USER. Supports idempotent requests using Idempotency-Key header.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) throws JsonProcessingException {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String requestHash = hashRequest(request.getEmail());
            Optional<String> cached = idempotencyService.checkAndCreate(idempotencyKey, requestHash);
            if (cached.isPresent()) {
                try {
                    AuthResponse cachedResponse = objectMapper.readValue(cached.get(), AuthResponse.class);
                    return ResponseEntity.ok(ApiResponse.success("User registered successfully (cached)", cachedResponse));
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached idempotency response: {}", e.getMessage());
                }
            }

            try {
                AuthResponse response = authService.register(request);
                String serialized = objectMapper.writeValueAsString(response);
                idempotencyService.complete(idempotencyKey, serialized);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("User registered successfully", response));
            } catch (Exception e) {
                idempotencyService.fail(idempotencyKey);
                throw e;
            }
        }

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @Operation(summary = "Authenticate user", description = "Logs in user with email and password, returning JWT access and refresh tokens.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token (with refresh token rotation).")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @Operation(summary = "Logout user", description = "Revokes the active refresh token and terminates the session.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @Operation(summary = "Forgot password request", description = "Sends an OTP to the user's registered email address for password reset.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset OTP has been sent to your email", null));
    }

    @Operation(summary = "Reset password", description = "Verifies the OTP and resets the password for the given email account.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully. Please log in with your new password.", null));
    }

    private String hashRequest(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }
}
