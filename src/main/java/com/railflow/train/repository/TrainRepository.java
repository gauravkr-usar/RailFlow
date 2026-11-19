package com.railflow.train.repository;

import com.railflow.train.entity.Train;
import com.railflow.station.entity.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    List<Train> findBySourceStationAndDestinationStation(Station sourceStation, Station destinationStation);

    @Query("SELECT t FROM Train t WHERE " +
           "LOWER(t.trainNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.trainName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Train> searchByTrainNumberOrName(@Param("query") String query, Pageable pageable);

    boolean existsByTrainNumber(String trainNumber);

    List<Train> findByIsActiveTrue();

    long countByIsActiveTrue();
}
