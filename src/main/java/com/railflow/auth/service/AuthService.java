package com.railflow.auth.service;

import com.railflow.auth.dto.*;
import com.railflow.auth.entity.OtpRecord;
import com.railflow.auth.entity.OtpRecord.OtpPurpose;
import com.railflow.auth.entity.RefreshToken;
import com.railflow.auth.repository.OtpRepository;
import com.railflow.auth.repository.RefreshTokenRepository;
import com.railflow.exception.DuplicateResourceException;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.security.JwtTokenProvider;
import com.railflow.user.entity.Role;
import com.railflow.user.entity.Role.RoleName;
import com.railflow.user.entity.User;
import com.railflow.user.repository.RoleRepository;
import com.railflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private static final long OTP_EXPIRY_MINUTES = 10;
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;
    private static final int OTP_LENGTH = 6;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpRepository otpRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Find or create USER role
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "USER"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .isEnabled(true)
                .isEmailVerified(false)
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered with email: {}", savedUser.getEmail());

        return generateTokenPair(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        // Revoke any existing refresh tokens
        refreshTokenRepository.revokeAllTokensByUser(user);

        return generateTokenPair(user);
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ResourceNotFoundException("RefreshToken", "token", "provided token"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token is expired or revoked. Please log in again.");
        }

        if (!jwtTokenProvider.validateToken(refreshTokenStr)) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new IllegalArgumentException("Invalid refresh token. Please log in again.");
        }

        User user = refreshToken.getUser();

        // Rotate refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateTokenPair(user);
    }

    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User {} logged out successfully", token.getUser().getEmail());
                });
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Invalidate old OTPs
        otpRepository.invalidateAllOtps(email, OtpPurpose.PASSWORD_RESET);

        String otp = generateOtp();
        OtpRecord otpRecord = OtpRecord.builder()
                .email(email)
                .otp(otp)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isUsed(false)
                .build();
        otpRepository.save(otpRecord);

        // TODO: integrate with email service to send OTP
        log.info("Password reset OTP generated for user: {} (OTP: {})", email, otp);
    }

    public void resetPassword(ResetPasswordRequest request) {
        OtpRecord otpRecord = otpRepository
                .findTopByEmailAndPurposeAndIsUsedFalseOrderByIdDesc(request.getEmail(), OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new ResourceNotFoundException("OTP", "email", request.getEmail()));

        if (!otpRecord.isValid()) {
            throw new IllegalArgumentException("OTP is expired or already used.");
        }
        if (!otpRecord.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP provided.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used and revoke all refresh tokens (force re-login)
        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);
        refreshTokenRepository.revokeAllTokensByUser(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    // ─── Private Helpers ────────────────────────────────────────────────────────

    private AuthResponse generateTokenPair(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY_DAYS * 24 * 3600))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
