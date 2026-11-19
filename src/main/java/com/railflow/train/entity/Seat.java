package com.railflow.train.entity;

import com.railflow.common.enums.BerthType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "berth_type", nullable = false)
    private BerthType berthType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @ToString.Exclude
    private Coach coach;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
