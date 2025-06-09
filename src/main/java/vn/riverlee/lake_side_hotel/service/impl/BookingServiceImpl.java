package vn.riverlee.lake_side_hotel.service.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.BookingRequest;
import vn.riverlee.lake_side_hotel.enums.BookingStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentType;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.model.Booking;
import vn.riverlee.lake_side_hotel.model.Room;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.repository.BookingRepository;
import vn.riverlee.lake_side_hotel.repository.RoomRepository;
import vn.riverlee.lake_side_hotel.repository.UserRepository;
import vn.riverlee.lake_side_hotel.service.BookingService;
import vn.riverlee.lake_side_hotel.service.EmailService;
import vn.riverlee.lake_side_hotel.util.CodeGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.priceInfo.service-fee:29}")
    private int serviceFee;

    @Value("${app.priceInfo.tax-rate:0.1}")
    private double taxRate;

    @Override
    public Long addBooking(BookingRequest request) throws BadRequestException, MessagingException {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + request.getRoomId() + " not found"));

        LocalDate checkInDate = request.getCheckInDate();
        LocalDate checkOutDate = request.getCheckOutDate();
        BigDecimal roomPrice = room.getPrice();
        long daysBetween = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal taxAmount = roomPrice
                .multiply(BigDecimal.valueOf(taxRate))
                .setScale(0, RoundingMode.HALF_UP); // tương tự Math.round
        BigDecimal reCalculatedTotalPrice = roomPrice
                .multiply(BigDecimal.valueOf(daysBetween))
                .add(taxAmount)
                .add(BigDecimal.valueOf(serviceFee));

        if (reCalculatedTotalPrice.compareTo(request.getTotalPrice()) != 0) {
            throw new BadRequestException("Total price mismatch. Please check your booking information.");
        }

        List<Booking> conflictBookings = bookingRepository.findAllByCheckInDateGreaterThanEqualAndCheckOutDateLessThanEqualAndRoomAndBookingStatus(
                checkInDate,
                checkOutDate,
                room,
                BookingStatus.CONFIRMED
        );

        if (room.getTotalRooms() <= conflictBookings.size()) {
            throw new BadRequestException("Room not available");
        }

        String confirmationCode = CodeGenerator.generateConfirmationCode();

        User user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof User) {
                user = (User) principal;
            }
        }

        Booking booking = Booking.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .tel(request.getTel())
                .numOfGuest(request.getNumOfGuest())
                .bookingStatus(BookingStatus.PENDING)
                .totalPrice(request.getTotalPrice())
                .confirmationCode(confirmationCode)
                .room(room)
                .user(user) // user có thể là null nếu không đăng nhập
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
