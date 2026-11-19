package com.railflow.inventory.entity;

import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.InventoryStatus;
import com.railflow.station.entity.Station;
import com.railflow.train.entity.Coach;
import com.railflow.train.entity.Seat;
import com.railflow.train.entity.Train;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_seat_inventory_segment", columnNames = {
                "train_id", "coach_id", "seat_id", "journey_date", "source_station_id", "destination_station_id"
        })
}, indexes = {
        @Index(name = "idx_seat_inv_search", columnList = "train_id, journey_date, coach_type, status, source_station_id, destination_station_id"),
        @Index(name = "idx_seat_inv_hold_expiry", columnList = "hold_expiry, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "train_run_id")
    private Long trainRunId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    @ToString.Exclude
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @ToString.Exclude
    private Coach coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    @ToString.Exclude
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_station_id", nullable = false)
    @ToString.Exclude
    private Station sourceStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_station_id", nullable = false)
    @ToString.Exclude
    private Station destinationStation;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false, length = 20)
    private CoachType coachType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Column(name = "fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(name = "hold_expiry")
    private LocalDateTime holdExpiry;

    @Column(name = "booking_id")
    private Long bookingId;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
