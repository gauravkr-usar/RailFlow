package com.railflow.cancellation.service;

import com.railflow.booking.entity.Booking;
import com.railflow.booking.entity.BookingPassenger;
import com.railflow.booking.repository.BookingPassengerRepository;
import com.railflow.booking.repository.BookingRepository;
import com.railflow.cancellation.dto.CancellationResponse;
import com.railflow.cancellation.entity.Cancellation;
import com.railflow.cancellation.repository.CancellationRepository;
import com.railflow.common.enums.BookingStatus;
import com.railflow.common.enums.PassengerStatus;
import com.railflow.common.enums.PaymentStatus;
import com.railflow.common.enums.RefundStatus;
import com.railflow.exception.BookingException;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.inventory.service.SeatInventoryService;
import com.railflow.payment.entity.Payment;
import com.railflow.payment.repository.PaymentRepository;
import com.railflow.payment.service.RazorpayService;
import com.railflow.refund.entity.Refund;
import com.railflow.refund.repository.RefundRepository;
import com.railflow.user.entity.User;
import com.railflow.waitinglist.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final CancellationRepository cancellationRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final SeatInventoryService seatInventoryService;
    private final RazorpayService razorpayService;
    private final WaitingListService waitingListService;

    @Transactional
    public CancellationResponse cancelBooking(Long bookingId, User user) {
        log.info("Processing cancellation for bookingId: {} by user: {}", bookingId, user.getEmail());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingException("Booking does not belong to the authenticated user");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        // Calculate charges
        BigDecimal originalFare = booking.getTotalFare();
        CancellationChargesInfo chargesInfo = calculateCancellationCharges(booking);
        BigDecimal cancellationCharges = chargesInfo.charges();
        BigDecimal refundAmount = originalFare.subtract(cancellationCharges).max(BigDecimal.ZERO);

        // 1. Release Inventory
        if (booking.getHoldToken() != null && !booking.getHoldToken().isBlank()) {
            try {
                seatInventoryService.releaseHold(booking.getHoldToken());
            } catch (Exception e) {
                log.warn("Hold release exception (may be already confirmed): {}", e.getMessage());
            }
        }

        // 2. Update passenger statuses
        if (booking.getBookingPassengers() != null) {
            for (BookingPassenger bp : booking.getBookingPassengers()) {
                bp.setStatus(PassengerStatus.CANCELLED);
            }
            bookingPassengerRepository.saveAll(booking.getBookingPassengers());
        }

        // 3. Process refund if payment succeeded
        Refund refund = null;
        Optional<Payment> paymentOpt = paymentRepository.findByBooking_Id(booking.getId());
        if (paymentOpt.isPresent() && paymentOpt.get().getStatus() == PaymentStatus.SUCCESS && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = paymentOpt.get();
            String rzpRefundId = null;
            try {
                rzpRefundId = razorpayService.initiateRefund(payment.getRazorpayPaymentId(), refundAmount);
            } catch (Exception e) {
                log.error("Failed to initiate automated Razorpay refund: {}", e.getMessage());
            }

            refund = Refund.builder()
                    .payment(payment)
                    .booking(booking)
                    .refundAmount(refundAmount)
                    .razorpayRefundId(rzpRefundId)
                    .status(rzpRefundId != null ? RefundStatus.COMPLETED : RefundStatus.INITIATED)
                    .reason("User cancelled ticket: " + chargesInfo.ruleDescription())
                    .initiatedAt(LocalDateTime.now())
                    .completedAt(rzpRefundId != null ? LocalDateTime.now() : null)
                    .build();
            refund = refundRepository.save(refund);

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        // 4. Create Cancellation record
        Cancellation cancellation = Cancellation.builder()
                .booking(booking)
                .cancelledBy(user.getId())
                .cancellationTime(LocalDateTime.now())
                .reason("Voluntary cancellation")
                .originalFare(originalFare)
                .cancellationCharges(cancellationCharges)
                .refundAmount(refundAmount)
                .refund(refund)
                .cancellationRule(chargesInfo.ruleDescription())
                .build();
        cancellationRepository.save(cancellation);

        // 5. Update Booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // 6. Automated Waiting List Promotion Engine
        try {
            int releasedCount = (booking.getBookingPassengers() != null && !booking.getBookingPassengers().isEmpty())
                    ? booking.getBookingPassengers().size() : 1;
            var promoted = waitingListService.promoteEligiblePassengers(
                    booking.getTrain().getId(),
                    booking.getJourneyDate(),
                    booking.getCoachType(),
                    releasedCount
            );
            if (promoted != null && !promoted.isEmpty()) {
                log.info("Auto-promoted {} waiting list passenger(s) on train {} following booking cancellation",
                        promoted.size(), booking.getTrain().getTrainNumber());
            }
        } catch (Exception ex) {
            log.warn("Non-fatal: could not process waiting list promotion: {}", ex.getMessage());
        }

        log.info("Booking {} successfully cancelled. Refund amount: {}", booking.getPnr(), refundAmount);

        return CancellationResponse.builder()
                .bookingId(booking.getId())
                .pnr(booking.getPnr())
                .originalFare(originalFare)
                .cancellationCharges(cancellationCharges)
                .refundAmount(refundAmount)
                .refundStatus(refund != null ? refund.getStatus() : null)
                .cancellationTime(cancellation.getCancellationTime())
                .build();
    }

    public CancellationChargesInfo calculateCancellationCharges(Booking booking) {
        LocalDateTime departureDateTime = LocalDateTime.of(
                booking.getJourneyDate(),
                booking.getTrain().getDepartureTime() != null ? booking.getTrain().getDepartureTime() : java.time.LocalTime.of(10, 0)
        );

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, departureDateTime);
        long hours = duration.toHours();

        BigDecimal fare = booking.getTotalFare();

        if (hours > 48) {
            BigDecimal minCharge = switch (booking.getCoachType()) {
                case ONE_A, TWO_A -> BigDecimal.valueOf(480);
                case THREE_A, CC, EC -> BigDecimal.valueOf(360);
                default -> BigDecimal.valueOf(240);
            };
            BigDecimal fivePercent = fare.multiply(BigDecimal.valueOf(0.05));
            BigDecimal charge = minCharge.max(fivePercent).setScale(2, RoundingMode.HALF_UP);
            return new CancellationChargesInfo(charge.min(fare), "> 48 hrs before departure: standard flat cancellation fee applies");
        } else if (hours >= 24) {
            BigDecimal charge = fare.multiply(BigDecimal.valueOf(0.25)).setScale(2, RoundingMode.HALF_UP);
            return new CancellationChargesInfo(charge, "24-48 hrs before departure: 25% cancellation charge");
        } else if (hours >= 12) {
            BigDecimal charge = fare.multiply(BigDecimal.valueOf(0.50)).setScale(2, RoundingMode.HALF_UP);
            return new CancellationChargesInfo(charge, "12-24 hrs before departure: 50% cancellation charge");
        } else {
            return new CancellationChargesInfo(fare, "< 12 hrs before departure: 100% cancellation charge (non-refundable)");
        }
    }

    public record CancellationChargesInfo(BigDecimal charges, String ruleDescription) {}
}
