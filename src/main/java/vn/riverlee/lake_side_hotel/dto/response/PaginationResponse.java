package vn.riverlee.lake_side_hotel.dto.response;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class PaginationResponse<T> {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private T items;
}