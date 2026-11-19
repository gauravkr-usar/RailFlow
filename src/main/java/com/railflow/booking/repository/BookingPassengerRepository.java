package com.railflow.booking.repository;

import com.railflow.booking.entity.Booking;
import com.railflow.booking.entity.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, Long> {
    List<BookingPassenger> findByBooking(Booking booking);
    List<BookingPassenger> findByBooking_Id(Long bookingId);
}
