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
public class EditRoomRequest implements Serializable {

    private String type;

    @Size(max = 255, message = "Summary must not exceed 255 characters")
    private String summary;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Area must be greater than 0")
    @Digits(integer = 6, fraction = 2, message = "Area format is invalid")
    private BigDecimal area;

    @Size(max = 100, message = "Beds field must not exceed 100 characters")
    private String beds;

    private List<@NotBlank(message = "Amenity must not be blank") String> amenities;

    private List<@NotBlank(message = "Feature must not be blank") String> features;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Average rating must be at least 0.0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Average rating must not exceed 5.0")
    @Digits(integer = 1, fraction = 2, message = "Average rating format is invalid")
    private BigDecimal avgRating;

    private MultipartFile thumbnail;

    private List<MultipartFile> images;

    @Min(value = 0, message = "Review count must not be negative")
    private Integer reviewCount;
}
