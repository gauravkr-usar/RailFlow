package com.railflow.booking.repository;

import com.railflow.booking.entity.Booking;
import com.railflow.common.enums.BookingStatus;
import com.railflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnr(String pnr);

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    Page<Booking> findByUser(User user, Pageable pageable);

    Optional<Booking> findByIdAndUser(Long id, User user);

    List<Booking> findByStatus(BookingStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :cutoff")
    List<Booking> findExpiredPendingBookings(@Param("status") BookingStatus status, @Param("cutoff") LocalDateTime cutoff);

    long countByStatus(BookingStatus status);
}
