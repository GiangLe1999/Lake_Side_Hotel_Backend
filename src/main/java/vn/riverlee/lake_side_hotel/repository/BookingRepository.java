package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.enums.BookingStatus;
import vn.riverlee.lake_side_hotel.model.Booking;
import vn.riverlee.lake_side_hotel.model.Room;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByCheckInDateGreaterThanEqualAndCheckOutDateLessThanEqualAndRoomAndBookingStatus(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Room room,
            BookingStatus bookingStatus
    );

    // Khi @Query là UPDATE/DELETE thì phải thêm @Modifying
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.bookingStatus = 'PENDING' AND b.createdAt < :cutoffTime")
    void deletePendingBookingsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}
