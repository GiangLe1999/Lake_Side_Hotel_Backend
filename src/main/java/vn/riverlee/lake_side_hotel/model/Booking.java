package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Booking")
@Table(name = "tbl_booking")
public class Booking extends AbstractEntity {
    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "guest_full_name")
    private String guestFullName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "nums_of_adults")
    private int numOfAdults;

    @Column(name = "nums_of_children")
    private int numOfChildren;

    @Column(name = "nums_of_guest")
    private int numOfGuest;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public void calculateNumOfAdults (int numOfAdults) {
        this.numOfAdults = numOfAdults;
        calculateNumOfGuest();
    }

    public void calculateNumOfChildren (int numOfChildren) {
        this.numOfChildren = numOfChildren;
        calculateNumOfGuest();
    }

    public void calculateNumOfGuest() {
        this.numOfGuest = this.numOfAdults + this.numOfChildren;
    }

    public static class BookingBuilder {
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private String guestFullName;
        private String guestEmail;
        private int numOfAdults;
        private int numOfChildren;
        private String confirmationCode;
        private Room room;

        public BookingBuilder checkInDate(LocalDate checkInDate) {
            this.checkInDate = checkInDate;
            return this;
        }

        public BookingBuilder checkOutDate(LocalDate checkOutDate) {
            this.checkOutDate = checkOutDate;
            return this;
        }

        public BookingBuilder guestFullName(String guestFullName) {
            this.guestFullName = guestFullName;
            return this;
        }

        public BookingBuilder guestEmail(String guestEmail) {
            this.guestEmail = guestEmail;
            return this;
        }

        public BookingBuilder numOfAdults(int numOfAdults) {
            this.numOfAdults = numOfAdults;
            return this;
        }

        public BookingBuilder numOfChildren(int numOfChildren) {
            this.numOfChildren = numOfChildren;
            return this;
        }

        public BookingBuilder confirmationCode(String confirmationCode) {
            this.confirmationCode = confirmationCode;
            return this;
        }

        public BookingBuilder room(Room room) {
            this.room = room;
            return this;
        }

        public Booking build() {
            Booking booking = new Booking();
            booking.setCheckInDate(this.checkInDate);
            booking.setCheckOutDate(this.checkOutDate);
            booking.setGuestFullName(this.guestFullName);
            booking.setGuestEmail(this.guestEmail);
            booking.setNumOfAdults(this.numOfAdults);
            booking.setNumOfChildren(this.numOfChildren);
            booking.setConfirmationCode(this.confirmationCode);
            booking.setRoom(this.room);
            booking.calculateNumOfGuest(); // đảm bảo numOfGuest được tính đúng
            return booking;
        }
    }

    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    /*
     * Booking booking = Booking.builder()
     *     .checkInDate(LocalDate.of(2025, 5, 20))
     *     .checkOutDate(LocalDate.of(2025, 5, 25))
     *     .guestFullName("Nguyen Van A")
     *     .guestEmail("a@gmail.com")
     *     .numOfAdults(2)
     *     .numOfChildren(1)
     *     .confirmationCode("CONF123")
     *     .room(room)
     *     .build();
     */
}
