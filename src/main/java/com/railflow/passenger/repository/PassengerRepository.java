package com.railflow.passenger.repository;

import com.railflow.passenger.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findByUserId(Long userId);

    List<Passenger> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Passenger> findByIdAndUserId(Long id, Long userId);
}
