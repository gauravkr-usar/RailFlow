package com.railflow.cancellation.repository;

import com.railflow.cancellation.entity.Cancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancellationRepository extends JpaRepository<Cancellation, Long> {

    Optional<Cancellation> findByBooking_Id(Long bookingId);
}
