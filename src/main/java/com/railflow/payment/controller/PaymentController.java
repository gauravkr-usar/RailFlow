package com.railflow.payment.controller;

import com.railflow.common.ApiResponse;
import com.railflow.payment.dto.CreatePaymentOrderRequest;
import com.railflow.payment.dto.PaymentOrderResponse;
import com.railflow.payment.dto.PaymentStatusResponse;
import com.railflow.payment.dto.VerifyPaymentRequest;
import com.railflow.payment.service.PaymentService;
import com.railflow.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Razorpay Payment Processing & Verification APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay payment order for booking")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request,
            @AuthenticationPrincipal User currentUser) {
        PaymentOrderResponse response = paymentService.createPaymentOrder(request.getBookingId(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment order created successfully", response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature and confirm booking")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal User currentUser) {
        PaymentStatusResponse response = paymentService.verifyPayment(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment status for booking")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @PathVariable Long bookingId) {
        PaymentStatusResponse response = paymentService.getPaymentStatusByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Payment status fetched", response));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay Webhook listener (idempotent)")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
