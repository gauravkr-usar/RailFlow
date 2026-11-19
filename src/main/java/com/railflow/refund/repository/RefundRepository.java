package com.railflow.refund.repository;

import com.railflow.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPayment_Id(Long paymentId);

    List<Refund> findByBooking_Id(Long bookingId);
}
