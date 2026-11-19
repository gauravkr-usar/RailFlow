package com.railflow.route.repository;

import com.railflow.route.entity.RouteStation;
import com.railflow.route.entity.TrainRoute;
import com.railflow.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, Long> {

    List<RouteStation> findByTrainRouteOrderBySequenceNumber(TrainRoute trainRoute);

    Optional<RouteStation> findByTrainRouteAndStation(TrainRoute trainRoute, Station station);

    @Query("SELECT DISTINCT rs1.trainRoute.train.id FROM RouteStation rs1, RouteStation rs2 " +
           "WHERE rs1.station.stationCode = :sourceCode " +
           "AND rs2.station.stationCode = :destCode " +
           "AND rs1.trainRoute = rs2.trainRoute " +
           "AND rs1.sequenceNumber < rs2.sequenceNumber")
    List<Long> findTrainIdsServingStations(
            @Param("sourceCode") String sourceCode,
            @Param("destCode") String destCode);
}
