package vn.riverlee.lake_side_hotel.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.riverlee.lake_side_hotel.dto.request.PaymentRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaymentResponse;
import vn.riverlee.lake_side_hotel.enums.BookingStatus;
import vn.riverlee.lake_side_hotel.enums.PaymentStatus;
import vn.riverlee.lake_side_hotel.model.*;
import vn.riverlee.lake_side_hotel.repository.BookingRepository;
import vn.riverlee.lake_side_hotel.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    public PaymentResponse createPaymentIntent(PaymentRequest request) throws StripeException {
        // Tìm booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Kiểm tra booking đã có payment chưa
        if (booking.getPayment() != null) {
            throw new RuntimeException("Payment already exists for this booking");
        }

        // Tạo PaymentIntent với Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Stripe dùng cents
                .setCurrency(request.getCurrency().toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("booking_id", booking.getId().toString())
                .putMetadata("customer_email", booking.getEmail())
                .putMetadata("room_name", booking.getRoom().getName())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Tạo Payment entity
        Payment payment = Payment.builder()
                .stripePaymentIntentId(paymentIntent.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .status(paymentIntent.getStatus())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();
    }

    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) throws StripeException {
        // Lấy thông tin từ Stripe
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        // Tìm payment trong database
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Cập nhật payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStripeChargeId(paymentIntent.getLatestCharge());
        payment.setPaymentMethod(paymentIntent.getPaymentMethod());

        // Cập nhật booking
        Booking booking = payment.getBooking();
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        messagingTemplate.convertAndSend(
                "/topic/payment/" + booking.getId(),
                "success"
        );

        log.info("Payment completed successfully for booking: {}", booking.getId());
    }

    @Transactional
    public void handlePaymentFailed(String paymentIntentId, String failureReason) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);

        Booking booking = payment.getBooking();
        booking.setPaymentStatus(PaymentStatus.FAILED);
        booking.setBookingStatus(BookingStatus.CANCELLED);

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        messagingTemplate.convertAndSend(
                "/topic/payment/" + booking.getId(),
                "failed"
        );

        log.error("Payment failed for booking: {}. Reason: {}", booking.getId(), failureReason);
    }

    @Transactional
    public void refundPayment(Long bookingId, BigDecimal refundAmount) throws StripeException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = booking.getPayment();
        if (payment == null || !payment.getStatus().equals(PaymentStatus.PAID)) {
            throw new RuntimeException("No completed payment found for this booking");
        }

        // Tạo refund với Stripe
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(payment.getStripeChargeId())
                .setAmount(refundAmount.multiply(BigDecimal.valueOf(100)).longValue())
                .build();

        Refund refund = Refund.create(params);

        // Cập nhật payment
        payment.setRefundAmount(payment.getRefundAmount().add(refundAmount));
        payment.setStatus(PaymentStatus.REFUNDED);

        // Cập nhật booking
        booking.setBookingStatus(BookingStatus.CANCELLED);

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        log.info("Refund processed for booking: {}. Amount: {}", booking.getId(), refundAmount);
    }
}