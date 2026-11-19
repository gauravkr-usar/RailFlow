package com.railflow.schedule.repository;

import com.railflow.schedule.entity.TrainSchedule;
import com.railflow.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {

    Optional<TrainSchedule> findByTrainAndJourneyDate(Train train, LocalDate journeyDate);

    Optional<TrainSchedule> findByTrainIdAndJourneyDate(Long trainId, LocalDate journeyDate);

    List<TrainSchedule> findByJourneyDateBetween(LocalDate fromDate, LocalDate toDate);

    List<TrainSchedule> findByTrainIdAndJourneyDateBetweenOrderByJourneyDateAsc(Long trainId, LocalDate fromDate, LocalDate toDate);

    boolean existsByTrainAndJourneyDate(Train train, LocalDate journeyDate);
}
