package com.railflow.route.entity;

import com.railflow.station.entity.Station;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "route_stations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"train_route_id", "sequence_number"}),
        @UniqueConstraint(columnNames = {"train_route_id", "station_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_route_id", nullable = false)
    @ToString.Exclude
    private TrainRoute trainRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    @ToString.Exclude
    private Station station;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "distance_from_source")
    private Integer distanceFromSource;

    @Column(name = "day_offset", nullable = false)
    @Builder.Default
    private Integer dayOffset = 0;

    @Column(name = "platform_number", length = 10)
    private String platformNumber;
}
