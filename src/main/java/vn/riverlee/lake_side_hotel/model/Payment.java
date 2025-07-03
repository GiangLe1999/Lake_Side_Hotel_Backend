package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.*;
import vn.riverlee.lake_side_hotel.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Payment")
@Table(name = "tbl_payment")
public class Payment extends AbstractEntity {
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string để lưu thêm thông tin

    // Quan hệ 1-1 với User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Quan hệ 1-1 với Booking
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}