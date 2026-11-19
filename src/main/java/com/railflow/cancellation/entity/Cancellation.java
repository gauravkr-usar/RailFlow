package com.railflow.cancellation.entity;

import com.railflow.booking.entity.Booking;
import com.railflow.refund.entity.Refund;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "cancelled_by", nullable = false)
    private Long cancelledBy;

    @Column(name = "cancellation_time", nullable = false)
    private LocalDateTime cancellationTime;

    private String reason;

    @Column(name = "original_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalFare;

    @Column(name = "cancellation_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal cancellationCharges;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id")
    private Refund refund;

    @Column(name = "cancellation_rule")
    private String cancellationRule;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
