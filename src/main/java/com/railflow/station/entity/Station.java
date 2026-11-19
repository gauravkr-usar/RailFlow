package com.railflow.station.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stations", uniqueConstraints = {
        @UniqueConstraint(columnNames = "station_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "station_code", nullable = false, unique = true, length = 10)
    private String stationCode;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "zone_code", length = 10)
    private String zoneCode;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
