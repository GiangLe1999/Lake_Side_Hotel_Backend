package vn.riverlee.lake_side_hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
public class PaymentResultResponse implements Serializable {
    String status;
    String message;
}
