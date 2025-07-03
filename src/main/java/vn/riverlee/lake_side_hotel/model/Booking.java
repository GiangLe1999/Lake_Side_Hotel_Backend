package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.*;
import vn.riverlee.lake_side_hotel.enums.BookingStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Booking")
@Table(name = "tbl_booking")
public class Booking extends AbstractEntity {
    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "tel")
    private String tel;

    @Column(name = "num_of_guest")
    private int numOfGuest;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
}
