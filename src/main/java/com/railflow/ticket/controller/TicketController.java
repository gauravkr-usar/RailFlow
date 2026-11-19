package com.railflow.ticket.controller;

import com.railflow.booking.entity.Booking;
import com.railflow.booking.repository.BookingRepository;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.ticket.service.PdfTicketGeneratorService;
import com.railflow.ticket.service.QrCodeGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket", description = "PDF Ticket Generation & Verification QR Code APIs")
public class TicketController {

    private final BookingRepository bookingRepository;
    private final PdfTicketGeneratorService pdfTicketGeneratorService;
    private final QrCodeGeneratorService qrCodeGeneratorService;

    @GetMapping("/{pnr}/pdf")
    @Operation(summary = "Download Electronic Reservation Slip (ERS) PDF Ticket")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable String pnr) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with PNR: " + pnr));

        byte[] pdfBytes = pdfTicketGeneratorService.generateTicketPdf(booking);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"RailFlow_Ticket_" + pnr + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{pnr}/qr")
    @Operation(summary = "Get Ticket Verification QR Code PNG image")
    public ResponseEntity<byte[]> getTicketQrCode(@PathVariable String pnr) {
        byte[] qrBytes = qrCodeGeneratorService.generateQrCodeImage("https://railflow.com/pnr/" + pnr, 250, 250);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}
