package com.railflow.train.repository;

import com.railflow.common.enums.CoachType;
import com.railflow.train.entity.Coach;
import com.railflow.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    List<Coach> findByTrain(Train train);

    List<Coach> findByTrainAndCoachType(Train train, CoachType coachType);

    List<Coach> findByTrainAndIsActiveTrue(Train train);

    boolean existsByTrainAndCoachNumber(Train train, String coachNumber);
}
