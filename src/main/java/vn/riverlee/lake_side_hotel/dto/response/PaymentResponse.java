package vn.riverlee.lake_side_hotel.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private String paymentIntentId;
    private String clientSecret;
    private String status;
    private BigDecimal amount;
    private String currency;
}