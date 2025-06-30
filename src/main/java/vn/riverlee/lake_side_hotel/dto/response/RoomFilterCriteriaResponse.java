package vn.riverlee.lake_side_hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class RoomFilterCriteriaResponse {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> roomTypes;
    private List<String> roomBeds;
    private List<String> amenities;
    private List<String> features;
    private List<Integer> occupancyTypes;
}
