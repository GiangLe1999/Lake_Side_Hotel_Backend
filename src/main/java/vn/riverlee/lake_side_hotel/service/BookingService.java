package vn.riverlee.lake_side_hotel.service;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.BookingRequest;

public interface BookingService {
    Long addBooking(@Valid BookingRequest request) throws BadRequestException, MessagingException;
    Long resendConfirmationCode(Long bookingId, @Valid BookingRequest request) throws MessagingException;
}
