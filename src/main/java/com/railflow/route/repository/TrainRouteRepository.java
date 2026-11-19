package com.railflow.route.repository;

import com.railflow.route.entity.RouteStation;
import com.railflow.route.entity.TrainRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRouteRepository extends JpaRepository<TrainRoute, Long> {

    Optional<TrainRoute> findByTrainId(Long trainId);

    @Query("SELECT rs FROM RouteStation rs " +
           "WHERE rs.trainRoute.train.id = :trainId " +
           "AND rs.sequenceNumber >= (" +
           "   SELECT s1.sequenceNumber FROM RouteStation s1 " +
           "   WHERE s1.trainRoute.train.id = :trainId AND s1.station.stationCode = :sourceCode" +
           ") " +
           "AND rs.sequenceNumber <= (" +
           "   SELECT s2.sequenceNumber FROM RouteStation s2 " +
           "   WHERE s2.trainRoute.train.id = :trainId AND s2.station.stationCode = :destCode" +
           ") " +
           "ORDER BY rs.sequenceNumber ASC")
    List<RouteStation> findRouteStationsBetween(
            @Param("trainId") Long trainId,
            @Param("sourceCode") String sourceCode,
            @Param("destCode") String destCode);
}
