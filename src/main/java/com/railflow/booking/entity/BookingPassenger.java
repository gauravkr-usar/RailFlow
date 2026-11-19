package com.railflow.booking.entity;

import com.railflow.common.enums.BerthType;
import com.railflow.common.enums.PassengerStatus;
import com.railflow.passenger.entity.Passenger;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @Column(name = "seat_inventory_id")
    private Long seatInventoryId;

    @Column(name = "coach_number", length = 10)
    private String coachNumber;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "berth_type", length = 30)
    private BerthType berthType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PassengerStatus status;

    @Column(name = "waiting_list_number")
    private Integer waitingListNumber;

    @Column(name = "rac_number")
    private Integer racNumber;

    @Column(name = "fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;
}
