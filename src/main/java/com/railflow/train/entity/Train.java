package com.railflow.train.entity;

import com.railflow.common.enums.DayOfWeekFlag;
import com.railflow.common.enums.TrainType;
import com.railflow.station.entity.Station;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trains", uniqueConstraints = {
        @UniqueConstraint(columnNames = "train_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "train_number", nullable = false, unique = true, length = 10)
    private String trainNumber;

    @Column(name = "train_name", nullable = false)
    private String trainName;

    @Enumerated(EnumType.STRING)
    @Column(name = "train_type", nullable = false)
    private TrainType trainType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_station_id", nullable = false)
    @ToString.Exclude
    private Station sourceStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_station_id", nullable = false)
    @ToString.Exclude
    private Station destinationStation;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    @ElementCollection(targetClass = DayOfWeekFlag.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "train_running_days", joinColumns = @JoinColumn(name = "train_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    @Builder.Default
    private Set<DayOfWeekFlag> runningDays = new HashSet<>();

    @Column(name = "total_distance")
    private Integer totalDistance;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Coach> coaches = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
