package com.railflow.train.repository;

import com.railflow.train.entity.Coach;
import com.railflow.train.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByCoach(Coach coach);

    List<Seat> findByCoachAndIsActiveTrue(Coach coach);

    Optional<Seat> findByCoachAndSeatNumber(Coach coach, Integer seatNumber);

    boolean existsByCoachAndSeatNumber(Coach coach, Integer seatNumber);
}
