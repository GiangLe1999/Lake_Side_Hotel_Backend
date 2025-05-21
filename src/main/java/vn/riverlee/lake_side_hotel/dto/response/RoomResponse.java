package vn.riverlee.lake_side_hotel.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.riverlee.lake_side_hotel.model.Booking;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class RoomResponse {
    private Long id;
    private String type;
    private BigDecimal price;
    private boolean isBooked;
    private String thumbnailKey;
    private List<String> imageKeys;
    private List<BookingResponse> bookings;
}
