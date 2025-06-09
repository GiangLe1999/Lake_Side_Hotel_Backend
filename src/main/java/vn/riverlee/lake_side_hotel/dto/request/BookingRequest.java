package vn.riverlee.lake_side_hotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BookingRequest implements Serializable {

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
    private String tel;

    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 6, message = "Number of guests must not exceed 6")
    private int numOfGuest;

    // value = "0.0": giá trị tối thiểu là 0.0
    // inclusive = false: không bao gồm giá trị 0.0, tức là chỉ chấp nhận số lớn hơn 0.0
    // Nếu muốn cho phép 0.0 là hợp lệ, chỉ cần đổi inclusive = true (hoặc bỏ luôn vì mặc định là true)
    @NotNull(message = "Total Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total Price must be greater than 0")
    private BigDecimal totalPrice;

    @NotNull(message = "Room ID is required")
    private Long roomId;
}
