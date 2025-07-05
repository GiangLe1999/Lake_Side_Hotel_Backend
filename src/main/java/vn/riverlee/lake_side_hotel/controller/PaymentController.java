package vn.riverlee.lake_side_hotel.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
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
import java.util.Map;

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
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Webhook parsing failed: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Chỉ xử lý PaymentIntent events
        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;

                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing webhook event {}: ", event.getType(), e);
            // Vẫn return 200 để Stripe không retry
            return ResponseEntity.ok("Event processing failed but acknowledged");
        }

        return ResponseEntity.ok("Success");
    }

    // Xử lý payment_intent.succeeded event
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            String paymentIntentId = extractPaymentIntentId(event);

            if (paymentIntentId != null) {
                log.info("Processing successful payment intent: {}", paymentIntentId);
                paymentService.handlePaymentSuccess(paymentIntentId);
            } else {
                log.warn("Could not extract PaymentIntent ID from succeeded event");
            }
        } catch (Exception e) {
            log.error("Error handling payment intent succeeded event", e);
        }
    }

    // Xử lý payment_intent.payment_failed event
    private void handlePaymentIntentFailed(Event event) {
        try {
            String paymentIntentId = extractPaymentIntentId(event);
            String failureReason = extractFailureReason(event);


            if (paymentIntentId != null) {
                log.info("Processing failed payment intent: {} - Reason: {}", paymentIntentId, failureReason);
                paymentService.handlePaymentFailed(paymentIntentId, failureReason);
            } else {
                log.warn("Could not extract PaymentIntent ID from failed event");
            }
        } catch (Exception e) {
            log.error("Error handling payment intent failed event", e);
        }
    }

    // Helper method để extract PaymentIntent ID từ event
    private String extractPaymentIntentId(Event event) {
        try {
            Event.Data eventData = event.getData();
            if (eventData != null) {
                // Sử dụng getObject() để lấy StripeObject
                StripeObject stripeObject = eventData.getObject();

                if (stripeObject instanceof PaymentIntent) {
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    String paymentIntentId = paymentIntent.getId();
                    log.info("Extracted PaymentIntent ID: {}", paymentIntentId);
                    return paymentIntentId;
                } else {
                    log.warn("Stripe object is not PaymentIntent: {}", stripeObject.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.error("Error extracting PaymentIntent ID from event", e);
        }
        return null;
    }

    // Helper method để extract failure reason từ event
    private String extractFailureReason(Event event) {
        try {
            Event.Data eventData = event.getData();

            if (eventData != null) {
                StripeObject stripeObject = eventData.getObject();

                if (stripeObject instanceof PaymentIntent) {
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;

                    if (paymentIntent.getLastPaymentError() != null) {
                        return paymentIntent.getLastPaymentError().getMessage();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract failure reason from event", e);
        }
        return "Unknown error";
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