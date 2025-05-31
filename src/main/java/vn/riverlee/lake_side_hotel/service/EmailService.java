package vn.riverlee.lake_side_hotel.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.riverlee.lake_side_hotel.dto.request.BookingRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${spring.mail.from}")
    private String emailFrom;

    public void sendBookingConfirmation(String confirmationCode, String roomType, BookingRequest booking) throws MessagingException {
        log.info("Send booking confirmation email to {}", booking.getNumOfGuest());
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Context context = new Context();
        context.setVariable("confirmationCode", confirmationCode);
        context.setVariable("roomType", roomType);
        context.setVariable("numberOfGuests", booking.getNumOfGuest());
        context.setVariable("checkinDate", booking.getCheckInDate());
        context.setVariable("checkoutDate", booking.getCheckOutDate());
        context.setVariable("guestName", booking.getFullName());
        context.setVariable("phoneNumber", booking.getTel());
        context.setVariable("email", booking.getEmail());

        String htmlContent = springTemplateEngine.process("booking-confirmation-email", context);

        helper.setFrom(emailFrom);
        helper.setTo(booking.getEmail());
        helper.setSubject("Booking Confirmation - LuxuryStay");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

