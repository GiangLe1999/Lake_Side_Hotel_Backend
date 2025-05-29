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

    @NotBlank(message = "Room type can not be empty")
    @Size(max = 100, message = "Room type must not exceed 100 characters")
    private String type;

    @NotBlank(message = "Room summary can not be empty")
    @Size(max = 255, message = "Summary must not exceed 255 characters")
    private String summary;

    @NotBlank(message = "Room description can not be empty")
    private String description;

    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    // Nếu client không truyền, nó sẽ có giá trị mặc định là 1, qua được validation
    private Integer totalRooms;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Area must be greater than 0")
    @Digits(integer = 6, fraction = 2, message = "Area format is invalid (max 6 digits before decimal, 2 after)")
    private BigDecimal area;

    @NotBlank(message = "Bed info can not be empty")
    @Size(max = 100, message = "Beds info must not exceed 100 characters")
    private String beds;

    @NotEmpty(message = "At least one amenity is required")
    private List<@NotBlank(message = "Amenity can not be blank")
    @Size(max = 50, message = "Amenity must not exceed 50 characters") String> amenities;

    @NotNull(message = "Thumbnail image is required")
    private MultipartFile thumbnail;

    @NotEmpty(message = "At least one image is required")
    private List<@NotNull(message = "Image file must not be null") MultipartFile> images;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid (max 10 digits before decimal, 2 after)")
    private BigDecimal price;
}
