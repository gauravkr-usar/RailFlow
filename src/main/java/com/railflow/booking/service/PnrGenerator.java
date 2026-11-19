package com.railflow.booking.service;

import com.railflow.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class PnrGenerator {

    private final BookingRepository bookingRepository;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        String pnr;
        int attempts = 0;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            pnr = String.valueOf(number);
            attempts++;
            if (attempts > 20) {
                pnr = "RF" + System.currentTimeMillis() % 100000000;
                break;
            }
        } while (bookingRepository.findByPnr(pnr).isPresent());

        return pnr;
    }
}
