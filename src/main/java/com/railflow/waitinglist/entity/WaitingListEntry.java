package com.railflow.waitinglist.entity;

import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.WaitingListStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_lists", indexes = {
        @Index(name = "idx_wl_lookup", columnList = "train_id, journey_date, coach_type, status, position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitingListEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false, length = 20)
    private CoachType coachType;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WaitingListStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
