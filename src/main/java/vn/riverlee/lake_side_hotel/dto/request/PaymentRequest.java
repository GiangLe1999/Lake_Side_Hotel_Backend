package vn.riverlee.lake_side_hotel.dto.request;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class PaymentRequest {
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
}