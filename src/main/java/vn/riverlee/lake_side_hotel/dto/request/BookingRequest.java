package vn.riverlee.lake_side_hotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BookingRequest implements Serializable {

    @NotBlank(message = "Room type can not be empty")
    @Size(max = 100, message = "Room type must not exceed 100 characters")
    private String type;

    @NotBlank(message = "Room summary can not be empty")
    @Size(max = 255, message = "Summary must not exceed 255 characters")
    private String summary;
}
