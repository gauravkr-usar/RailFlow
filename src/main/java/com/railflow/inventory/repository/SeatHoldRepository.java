package com.railflow.inventory.repository;

import com.railflow.inventory.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Optional<SeatHold> findByHoldToken(String holdToken);

    List<SeatHold> findByExpiresAtBeforeAndIsReleasedFalse(LocalDateTime now);

    List<SeatHold> findByUserId(Long userId);
}
