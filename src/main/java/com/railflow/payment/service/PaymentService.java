package com.railflow.payment.service;

import com.railflow.booking.entity.Booking;
import com.railflow.booking.repository.BookingRepository;
import com.railflow.booking.service.BookingService;
import com.railflow.common.enums.BookingStatus;
import com.railflow.common.enums.PaymentStatus;
import com.railflow.exception.PaymentException;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.payment.dto.PaymentOrderResponse;
import com.railflow.payment.dto.PaymentStatusResponse;
import com.railflow.payment.dto.VerifyPaymentRequest;
import com.railflow.payment.entity.Payment;
import com.railflow.payment.event.PaymentFailedEvent;
import com.railflow.payment.event.PaymentSuccessfulEvent;
import com.railflow.payment.repository.PaymentRepository;
import com.railflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RazorpayService razorpayService;
    private final BookingService bookingService;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Value("${razorpay.key-id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Transactional
    public PaymentOrderResponse createPaymentOrder(Long bookingId, User user) {
        log.info("Creating payment order for bookingId: {} by user: {}", bookingId, user.getEmail());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new PaymentException("Booking does not belong to the authenticated user");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new PaymentException("Booking is already confirmed and paid for");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new PaymentException("Cannot pay for a cancelled booking");
        }

        // Check for existing pending payment
        Optional<Payment> existingPayment = paymentRepository.findByBooking_Id(bookingId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
            Payment p = existingPayment.get();
            return PaymentOrderResponse.builder()
                    .orderId(p.getRazorpayOrderId())
                    .amount(p.getAmount())
                    .currency(p.getCurrency())
                    .razorpayKeyId(razorpayKeyId)
                    .bookingId(booking.getId())
                    .pnr(booking.getPnr())
                    .build();
        }

        String razorpayOrderId = razorpayService.createOrder(booking.getId(), booking.getTotalFare());

        Payment payment = Payment.builder()
                .booking(booking)
                .razorpayOrderId(razorpayOrderId)
                .amount(booking.getTotalFare())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .attemptCount(1)
                .webhookProcessed(false)
                .build();

        paymentRepository.save(payment);

        return PaymentOrderResponse.builder()
                .orderId(razorpayOrderId)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .razorpayKeyId(razorpayKeyId)
                .bookingId(booking.getId())
                .pnr(booking.getPnr())
                .build();
    }

    @Transactional
    public PaymentStatusResponse verifyPayment(VerifyPaymentRequest request, User user) {
        log.info("Verifying payment for orderId: {}", request.getRazorpayOrderId());

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for orderId: " + request.getRazorpayOrderId()));

        // Idempotency: if already processed
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment for orderId {} was already verified and marked SUCCESS", request.getRazorpayOrderId());
            return mapToPaymentResponse(payment);
        }

        boolean isValid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid signature verification");
            paymentRepository.save(payment);

            publishPaymentFailedEvent(payment, "Invalid Razorpay payment signature");
            throw new PaymentException("Invalid Razorpay payment signature");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setWebhookProcessed(true);
        Payment savedPayment = paymentRepository.save(payment);

        // Confirm booking
        bookingService.confirmBooking(savedPayment.getBooking().getId());

        // Publish Kafka event
        publishPaymentSuccessfulEvent(savedPayment);

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public void processWebhook(String webhookBody, String webhookSignature) {
        log.info("Processing Razorpay webhook");
        // Simulated / production webhook processing
        // Can parse event JSON and mark payment SUCCESS or FAILED
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatusByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for booking: " + bookingId));
        return mapToPaymentResponse(payment);
    }

    private PaymentStatusResponse mapToPaymentResponse(Payment p) {
        return PaymentStatusResponse.builder()
                .paymentId(p.getId())
                .bookingId(p.getBooking() != null ? p.getBooking().getId() : null)
                .status(p.getStatus())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private void publishPaymentSuccessfulEvent(Payment payment) {
        try {
            kafkaTemplate.ifPresent(kt -> {
                PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(
                        payment.getBooking().getId(),
                        payment.getBooking().getPnr(),
                        payment.getBooking().getUser().getId(),
                        payment.getBooking().getUser().getEmail(),
                        payment.getAmount(),
                        payment.getRazorpayPaymentId(),
                        Instant.now()
                );
                kt.send("payment-successful-events", payment.getRazorpayOrderId(), event);
            });
        } catch (Exception e) {
            log.warn("Could not publish PaymentSuccessfulEvent to Kafka: {}", e.getMessage());
        }
    }

    private void publishPaymentFailedEvent(Payment payment, String reason) {
        try {
            kafkaTemplate.ifPresent(kt -> {
                PaymentFailedEvent event = new PaymentFailedEvent(
                        payment.getBooking().getId(),
                        payment.getBooking().getPnr(),
                        payment.getBooking().getUser().getId(),
                        reason,
                        Instant.now()
                );
                kt.send("payment-failed-events", payment.getRazorpayOrderId(), event);
            });
        } catch (Exception e) {
            log.warn("Could not publish PaymentFailedEvent to Kafka: {}", e.getMessage());
        }
    }
}
