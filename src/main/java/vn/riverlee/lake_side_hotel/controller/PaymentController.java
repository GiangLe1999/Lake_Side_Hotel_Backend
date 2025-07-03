package vn.riverlee.lake_side_hotel.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.PaymentRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaymentResponse;
import vn.riverlee.lake_side_hotel.service.PaymentService;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.createPaymentIntent(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating payment intent: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Xử lý các event từ Stripe
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null) {
                    try {
                        paymentService.handlePaymentSuccess(paymentIntent.getId());
                    } catch (Exception e) {
                        log.error("Error handling payment success: ", e);
                    }
                }
                break;

            case "payment_intent.payment_failed":
                PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (failedIntent != null) {
                    String failureReason = failedIntent.getLastPaymentError() != null ?
                            failedIntent.getLastPaymentError().getMessage() : "Unknown error";
                    paymentService.handlePaymentFailed(failedIntent.getId(), failureReason);
                }
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<String> refundPayment(
            @PathVariable Long bookingId,
            @RequestParam BigDecimal amount) {
        try {
            paymentService.refundPayment(bookingId, amount);
            return ResponseEntity.ok("Refund processed successfully");
        } catch (StripeException e) {
            log.error("Stripe refund error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refund failed");
        } catch (Exception e) {
            log.error("Error processing refund: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }
    }
}