package com.railflow.waitinglist.repository;

import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.WaitingListStatus;
import com.railflow.waitinglist.entity.WaitingListEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    List<WaitingListEntry> findByTrainIdAndJourneyDateAndCoachTypeAndStatusOrderByPositionAsc(
            Long trainId, LocalDate journeyDate, CoachType coachType, WaitingListStatus status
    );

    @Query("SELECT MAX(w.position) FROM WaitingListEntry w WHERE w.trainId = :trainId AND w.journeyDate = :journeyDate AND w.coachType = :coachType AND w.status = :status")
    Optional<Integer> findMaxPosition(
            @Param("trainId") Long trainId,
            @Param("journeyDate") LocalDate journeyDate,
            @Param("coachType") CoachType coachType,
            @Param("status") WaitingListStatus status
    );
}
