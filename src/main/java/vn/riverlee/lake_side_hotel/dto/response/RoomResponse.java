package vn.riverlee.lake_side_hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomResponse {
    private Long id;
    private String type;
    private String summary;
    private String description;
    private BigDecimal area;
    private String beds;
    private List<String> amenities;
    private BigDecimal price;
    private boolean isBooked;
    private String thumbnailKey;
    private List<String> imageKeys;
    private List<BookingResponse> bookings;
    private Date createdAt;
}
