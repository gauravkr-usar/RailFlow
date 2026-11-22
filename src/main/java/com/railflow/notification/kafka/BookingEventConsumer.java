package com.railflow.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.railflow.booking.entity.Booking;
import com.railflow.booking.event.BookingCancelledEvent;
import com.railflow.booking.event.BookingConfirmedEvent;
import com.railflow.booking.repository.BookingRepository;
import com.railflow.notification.entity.Notification;
import com.railflow.notification.entity.NotificationStatus;
import com.railflow.notification.entity.NotificationType;
import com.railflow.notification.repository.NotificationRepository;
import com.railflow.notification.service.EmailService;
import com.railflow.ticket.service.PdfTicketGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class BookingEventConsumer {

    private final BookingRepository bookingRepository;
    private final PdfTicketGeneratorService pdfTicketGeneratorService;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-confirmed-events", groupId = "railflow-notification-group")
    public void handleBookingConfirmed(String message) {
        log.info("Received BookingConfirmedEvent message: {}", message);
        try {
            BookingConfirmedEvent event = objectMapper.readValue(message, BookingConfirmedEvent.class);
            Booking booking = bookingRepository.findByPnr(event.pnr()).orElse(null);

            byte[] pdfBytes = null;
            if (booking != null) {
                pdfBytes = pdfTicketGeneratorService.generateTicketPdf(booking);
            }

            emailService.sendBookingConfirmationEmail(event.userEmail(), event.pnr(), pdfBytes);

            Notification notification = Notification.builder()
                    .userId(event.userId())
                    .recipient(event.userEmail())
                    .type(NotificationType.EMAIL)
                    .subject("Ticket Confirmed: " + event.pnr())
                    .content("Booking confirmed with total fare INR " + event.totalFare())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to process BookingConfirmedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "booking-cancelled-events", groupId = "railflow-notification-group")
    public void handleBookingCancelled(String message) {
        log.info("Received BookingCancelledEvent message: {}", message);
        try {
            BookingCancelledEvent event = objectMapper.readValue(message, BookingCancelledEvent.class);
            Notification notification = Notification.builder()
                    .userId(event.userId())
                    .recipient("User ID: " + event.userId())
                    .type(NotificationType.EMAIL)
                    .subject("Ticket Cancelled: " + event.pnr())
                    .content("Booking cancelled. Refund amount: INR " + event.refundAmount())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to process BookingCancelledEvent: {}", e.getMessage(), e);
        }
    }
}
