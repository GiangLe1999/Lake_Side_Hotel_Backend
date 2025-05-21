package vn.riverlee.lake_side_hotel.dto.request;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RoomRequest implements Serializable {
    @NotBlank(message = "Room type can not empty")
    private String type;

    @NotNull(message = "Thumbnail image is required")
    private MultipartFile thumbnail;

    @NotEmpty(message = "At least one image is required")
    private List<MultipartFile> images;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    // Phần nguyên có tối đa 10 chữ số
    // Phần thập phân có tối đa 2 chữ số
    // Vì Jackson cố gắng convert các String → BigDecimal, nên nếu truyền các chuỗi không hợp lệ như "abc"chẳng hạn
    // Vì Request thuộc dạng form-data nên nếu price không convert được sang BigDecimal:
    // Spring đánh dấu đó là binding error
    // Nếu có bất kỳ binding error nào → nó gói lại thành MethodArgumentNotValidException
    private BigDecimal price;
}


