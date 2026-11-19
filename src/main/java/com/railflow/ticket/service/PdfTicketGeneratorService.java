package com.railflow.ticket.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.railflow.booking.entity.Booking;
import com.railflow.booking.entity.BookingPassenger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfTicketGeneratorService {

    private final QrCodeGeneratorService qrCodeGeneratorService;

    public byte[] generateTicketPdf(Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(26, 54, 93));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(113, 128, 150));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

            // 1. Header
            Paragraph header = new Paragraph("RAILFLOW - ELECTRONIC RESERVATION SLIP (ERS)", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("IRCTC-Inspired High-Concurrency Railway Booking Platform", subTitleFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(15);
            document.add(subHeader);

            // 2. Journey & PNR Table
            PdfPTable journeyTable = new PdfPTable(4);
            journeyTable.setWidthPercentage(100);
            journeyTable.setSpacingAfter(10);

            addHeaderCell(journeyTable, "PNR Number", headerFont);
            addHeaderCell(journeyTable, "Train No. & Name", headerFont);
            addHeaderCell(journeyTable, "Journey Date", headerFont);
            addHeaderCell(journeyTable, "Class", headerFont);

            addCell(journeyTable, booking.getPnr(), boldCellFont);
            addCell(journeyTable, (booking.getTrain() != null ? booking.getTrain().getTrainNumber() + " - " + booking.getTrain().getTrainName() : "N/A"), cellFont);
            addCell(journeyTable, (booking.getJourneyDate() != null ? booking.getJourneyDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A"), cellFont);
            addCell(journeyTable, (booking.getCoachType() != null ? booking.getCoachType().name() : "N/A"), cellFont);

            document.add(journeyTable);

            // 3. Station & Time Table
            PdfPTable stationTable = new PdfPTable(4);
            stationTable.setWidthPercentage(100);
            stationTable.setSpacingAfter(10);

            addHeaderCell(stationTable, "From Station", headerFont);
            addHeaderCell(stationTable, "Departure", headerFont);
            addHeaderCell(stationTable, "To Station", headerFont);
            addHeaderCell(stationTable, "Arrival", headerFont);

            addCell(stationTable, (booking.getSourceStation() != null ? booking.getSourceStation().getStationName() + " (" + booking.getSourceStation().getStationCode() + ")" : "N/A"), cellFont);
            addCell(stationTable, (booking.getTrain() != null && booking.getTrain().getDepartureTime() != null ? booking.getTrain().getDepartureTime().toString() : "06:00"), cellFont);
            addCell(stationTable, (booking.getDestinationStation() != null ? booking.getDestinationStation().getStationName() + " (" + booking.getDestinationStation().getStationCode() + ")" : "N/A"), cellFont);
            addCell(stationTable, (booking.getTrain() != null && booking.getTrain().getArrivalTime() != null ? booking.getTrain().getArrivalTime().toString() : "14:00"), cellFont);

            document.add(stationTable);

            // 4. Passenger Details Table
            PdfPTable passTable = new PdfPTable(6);
            passTable.setWidthPercentage(100);
            passTable.setSpacingAfter(15);

            addHeaderCell(passTable, "#", headerFont);
            addHeaderCell(passTable, "Passenger Name", headerFont);
            addHeaderCell(passTable, "Age / Gender", headerFont);
            addHeaderCell(passTable, "Booking Status", headerFont);
            addHeaderCell(passTable, "Coach", headerFont);
            addHeaderCell(passTable, "Seat / Berth", headerFont);

            if (booking.getBookingPassengers() != null) {
                int idx = 1;
                for (BookingPassenger bp : booking.getBookingPassengers()) {
                    addCell(passTable, String.valueOf(idx++), cellFont);
                    addCell(passTable, bp.getPassenger() != null ? bp.getPassenger().getName() : "Passenger", boldCellFont);
                    addCell(passTable, (bp.getPassenger() != null ? bp.getPassenger().getAge() + " / " + bp.getPassenger().getGender() : "N/A"), cellFont);
                    addCell(passTable, bp.getStatus() != null ? bp.getStatus().name() : "CNF", boldCellFont);
                    addCell(passTable, bp.getCoachNumber() != null ? bp.getCoachNumber() : "-", cellFont);
                    addCell(passTable, (bp.getSeatNumber() != null && bp.getSeatNumber() > 0 ? String.valueOf(bp.getSeatNumber()) : "-") + (bp.getBerthType() != null ? " (" + bp.getBerthType() + ")" : ""), cellFont);
                }
            }
            document.add(passTable);

            // 5. Fare Summary Table & QR Code side-by-side
            PdfPTable bottomTable = new PdfPTable(2);
            bottomTable.setWidthPercentage(100);
            bottomTable.setWidths(new float[]{60, 40});

            PdfPTable fareTable = new PdfPTable(2);
            fareTable.setWidthPercentage(100);
            addHeaderCell(fareTable, "Fare Component", headerFont);
            addHeaderCell(fareTable, "Amount (INR)", headerFont);

            addCell(fareTable, "Ticket Fare", cellFont);
            addCell(fareTable, "Rs. " + (booking.getTotalFare() != null ? booking.getTotalFare() : BigDecimal.ZERO), cellFont);

            addCell(fareTable, "Convenience Fee (incl. GST)", cellFont);
            addCell(fareTable, "Rs. 17.70", cellFont);

            addCell(fareTable, "Total Amount Paid", boldCellFont);
            BigDecimal totalWithFee = (booking.getTotalFare() != null ? booking.getTotalFare().add(new BigDecimal("17.70")) : new BigDecimal("17.70"));
            addCell(fareTable, "Rs. " + totalWithFee, boldCellFont);

            PdfPCell fareCell = new PdfPCell(fareTable);
            fareCell.setBorder(Rectangle.NO_BORDER);
            bottomTable.addCell(fareCell);

            // Generate and embed QR code
            try {
                String qrData = "https://railflow.com/pnr/" + booking.getPnr();
                byte[] qrBytes = qrCodeGeneratorService.generateQrCodeImage(qrData, 120, 120);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.setAlignment(Element.ALIGN_CENTER);

                PdfPCell qrCell = new PdfPCell();
                qrCell.setBorder(Rectangle.NO_BORDER);
                qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qrCell.addElement(qrImage);

                Paragraph qrCaption = new Paragraph("Scan to verify PNR: " + booking.getPnr(), FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
                qrCaption.setAlignment(Element.ALIGN_CENTER);
                qrCell.addElement(qrCaption);

                bottomTable.addCell(qrCell);
            } catch (Exception e) {
                log.warn("Could not embed QR code in PDF: {}", e.getMessage());
                PdfPCell blank = new PdfPCell(new Paragraph(""));
                blank.setBorder(Rectangle.NO_BORDER);
                bottomTable.addCell(blank);
            }

            document.add(bottomTable);

            // 6. Footer instructions
            Paragraph footer = new Paragraph("\nImportant Instructions:\n1. Please carry a valid original Government Photo ID (Aadhaar, Passport, PAN Card, Voter ID) during the journey.\n2. E-ticket passenger can travel without physical printout by showing SMS or QR code on mobile device.\n3. In case of train cancellation, full refund is automatically credited to the original payment source.", FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY));
            footer.setSpacingBefore(15);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate ticket PDF for booking PNR {}: {}", booking.getPnr(), e.getMessage(), e);
            throw new RuntimeException("Error generating ticket PDF", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(26, 54, 93));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
