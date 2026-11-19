package com.railflow.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Optional<JavaMailSender> mailSender;

    public void sendBookingConfirmationEmail(String to, String pnr, byte[] pdfTicket) {
        if (mailSender.isEmpty()) {
            log.info("[MOCK EMAIL] To: {} | Subject: Booking Confirmation for PNR: {} | Attached PDF Ticket: {} bytes",
                    to, pnr, (pdfTicket != null ? pdfTicket.length : 0));
            return;
        }

        try {
            JavaMailSender sender = mailSender.get();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Your RailFlow Ticket is Confirmed! PNR: " + pnr);
            helper.setText("Dear Passenger,\n\nYour train ticket has been successfully booked.\nPNR: " + pnr +
                    "\nPlease find your attached Electronic Reservation Slip (ERS).\n\nWish you a pleasant journey!\nRailFlow Team");

            if (pdfTicket != null && pdfTicket.length > 0) {
                helper.addAttachment("RailFlow_Ticket_" + pnr + ".pdf", new ByteArrayResource(pdfTicket));
            }

            sender.send(message);
            log.info("Booking confirmation email sent to {} for PNR {}", to, pnr);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOtpEmail(String to, String otp, String purpose) {
        if (mailSender.isEmpty()) {
            log.info("[MOCK EMAIL] To: {} | OTP: {} | Purpose: {}", to, otp, purpose);
            return;
        }

        try {
            JavaMailSender sender = mailSender.get();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setTo(to);
            helper.setSubject("RailFlow OTP for " + purpose);
            helper.setText("Your verification OTP is: " + otp + "\nValid for 5 minutes. Do not share with anyone.");

            sender.send(message);
            log.info("OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }
}
