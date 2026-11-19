package com.railflow.inventory.repository;

import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.InventoryStatus;
import com.railflow.inventory.entity.SeatInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatInventory s WHERE s.id = :id")
    Optional<SeatInventory> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT s FROM SeatInventory s WHERE s.train.id = :trainId AND s.journeyDate = :date AND s.coachType = :coachType AND s.status = com.railflow.common.enums.InventoryStatus.AVAILABLE AND s.sourceStation.id = :srcId AND s.destinationStation.id = :destId ORDER BY s.coach.coachNumber, s.seat.seatNumber")
    List<SeatInventory> findAvailableSeats(@Param("trainId") Long trainId,
                                           @Param("date") LocalDate date,
                                           @Param("coachType") CoachType coachType,
                                           @Param("srcId") Long srcId,
                                           @Param("destId") Long destId);

    @Query("SELECT COUNT(s) FROM SeatInventory s WHERE s.train.id = :trainId AND s.journeyDate = :date AND s.coachType = :coachType AND s.status = com.railflow.common.enums.InventoryStatus.AVAILABLE AND s.sourceStation.id = :srcId AND s.destinationStation.id = :destId")
    long countAvailableSeats(@Param("trainId") Long trainId,
                             @Param("date") LocalDate date,
                             @Param("coachType") CoachType coachType,
                             @Param("srcId") Long srcId,
                             @Param("destId") Long destId);

    @Query("SELECT COUNT(s) FROM SeatInventory s WHERE s.train.id = :trainId AND s.journeyDate = :date AND s.coachType = :coachType AND s.sourceStation.id = :srcId AND s.destinationStation.id = :destId")
    long countTotalSeats(@Param("trainId") Long trainId,
                         @Param("date") LocalDate date,
                         @Param("coachType") CoachType coachType,
                         @Param("srcId") Long srcId,
                         @Param("destId") Long destId);

    @Modifying
    @Query("UPDATE SeatInventory s SET s.status = com.railflow.common.enums.InventoryStatus.AVAILABLE, s.holdExpiry = null, s.bookingId = null WHERE s.holdExpiry < :now AND s.status = com.railflow.common.enums.InventoryStatus.HELD")
    int releaseExpiredHolds(@Param("now") LocalDateTime now);

    List<SeatInventory> findByTrainIdAndJourneyDateAndSourceStationIdAndDestinationStationId(
            Long trainId, LocalDate journeyDate, Long sourceStationId, Long destinationStationId);

    List<SeatInventory> findByTrainIdAndJourneyDateAndCoachTypeAndSourceStationIdAndDestinationStationId(
            Long trainId, LocalDate journeyDate, CoachType coachType, Long sourceStationId, Long destinationStationId);

    boolean existsByTrainIdAndJourneyDateAndSourceStationIdAndDestinationStationId(
            Long trainId, LocalDate journeyDate, Long sourceStationId, Long destinationStationId);

    List<SeatInventory> findByBookingId(Long bookingId);
}
