package vn.riverlee.lake_side_hotel.service.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.BookingRequest;
import vn.riverlee.lake_side_hotel.enums.BookingStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentType;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.model.Booking;
import vn.riverlee.lake_side_hotel.model.Room;
import vn.riverlee.lake_side_hotel.repository.BookingRepository;
import vn.riverlee.lake_side_hotel.repository.RoomRepository;
import vn.riverlee.lake_side_hotel.service.BookingService;
import vn.riverlee.lake_side_hotel.service.EmailService;
import vn.riverlee.lake_side_hotel.util.CodeGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Value("${priceInfo.serviceFee}")
    private String serviceFee;

    @Value("${priceInfo.taxes}")
    private String taxes;

    @Override
    public Long addBooking(BookingRequest request) throws BadRequestException, MessagingException {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + request.getRoomId() + " not found"));

        List<Booking> conflictBookings = bookingRepository.findAllByCheckInDateGreaterThanEqualAndCheckOutDateLessThanEqualAndRoomAndBookingStatus(
                request.getCheckInDate(),
                request.getCheckOutDate(),
                room,
                BookingStatus.CONFIRMED
                );

        if (room.getTotalRooms() < conflictBookings.size()) {
            throw new BadRequestException("Room not available");
        }

        String confirmationCode = CodeGenerator.generateConfirmationCode();

        Booking booking = Booking.builder()
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .tel(request.getTel())
                .numOfGuest(request.getNumOfGuest())
                .bookingStatus(BookingStatus.PENDING)
                .totalPrice(request.getTotalPrice())
                .confirmationCode(confirmationCode)
                .room(room)
                // Cần tìm user
                .user(null)
                .build();

        emailService.sendBookingConfirmation(confirmationCode, room.getType(), request);

        return bookingRepository.save(booking).getId();
    }

    @Override
    public Long resendConfirmationCode(Long bookingId, BookingRequest request) throws MessagingException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        String newConfirmationCode = CodeGenerator.generateConfirmationCode();
        booking.setConfirmationCode(newConfirmationCode);
        emailService.sendBookingConfirmation(newConfirmationCode, booking.getRoom().getType(), request);
        return bookingRepository.save(booking).getId();
    }

    @Override
    public Long confirmBooking(long bookingId, String confirmationCode) throws BadRequestException {
        Booking booking = bookingRepository.findByIdAndConfirmationCodeAndBookingStatus(bookingId, confirmationCode, BookingStatus.PENDING)
                .orElseThrow(() -> new BadRequestException("The confirmation code you entered is incorrect"));

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking).getId();
    }

    @Override
    public PaymentType choosePaymentMethod(long bookingId, String paymentMethod) throws BadRequestException {
        Booking booking = bookingRepository.findByIdAndBookingStatus(bookingId, BookingStatus.CONFIRMED)
                .orElseThrow(() -> new BadRequestException("Booking not found"));

        PaymentType paymentType;
        try {
            paymentType = PaymentType.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported payment method: " + paymentMethod);
        }

        booking.setPaymentType(paymentType);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        return bookingRepository.save(booking).getPaymentType();
    }

    @Transactional
    @Scheduled(fixedRate = 3600000) // Chạy mỗi 1 tiếng
    public void deleteOldPendingBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        bookingRepository.deletePendingBookingsOlderThan(cutoffTime);
    }
}
