package com.railflow.inventory.entity;

import com.railflow.common.enums.CoachType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_holds", indexes = {
        @Index(name = "idx_seat_hold_token", columnList = "hold_token", unique = true),
        @Index(name = "idx_seat_hold_expiry", columnList = "expires_at, is_released")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "hold_token", nullable = false, unique = true, length = 64)
    private String holdToken;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "seat_hold_inventories", joinColumns = @JoinColumn(name = "seat_hold_id"))
    @Column(name = "inventory_id")
    @Builder.Default
    private List<Long> inventoryIds = new ArrayList<>();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(name = "source_station_id", nullable = false)
    private Long sourceStationId;

    @Column(name = "dest_station_id", nullable = false)
    private Long destStationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false, length = 20)
    private CoachType coachType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_released", nullable = false)
    @Builder.Default
    private boolean isReleased = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
