package com.railflow.schedule.repository;

import com.railflow.schedule.entity.TrainRun;
import com.railflow.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRunRepository extends JpaRepository<TrainRun, Long> {

    Optional<TrainRun> findByTrainAndRunDate(Train train, LocalDate runDate);

    Optional<TrainRun> findByTrainIdAndRunDate(Long trainId, LocalDate runDate);

    List<TrainRun> findByTrainIdAndRunDateBetween(Long trainId, LocalDate fromDate, LocalDate toDate);

    boolean existsByTrainAndRunDate(Train train, LocalDate runDate);
}
