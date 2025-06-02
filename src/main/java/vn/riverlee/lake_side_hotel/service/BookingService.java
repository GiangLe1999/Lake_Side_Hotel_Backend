package vn.riverlee.lake_side_hotel.service;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.coyote.BadRequestException;
import vn.riverlee.lake_side_hotel.dto.request.BookingRequest;
import vn.riverlee.lake_side_hotel.enums.PaymentType;

public interface BookingService {
    Long addBooking(@Valid BookingRequest request) throws BadRequestException, MessagingException;

    Long resendConfirmationCode(Long bookingId, @Valid BookingRequest request) throws MessagingException;

    Long confirmBooking(@Min(1) long id, @NotBlank @Size(min = 6, max = 6) String confirmationCode) throws BadRequestException;

    PaymentType choosePaymentMethod(@Min(1) long id, String paymentMethod) throws BadRequestException;
}
