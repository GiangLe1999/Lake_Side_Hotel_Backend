package vn.riverlee.lake_side_hotel.dto.response;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class PaginationResponse<T> {
    private boolean hasNextPage;
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalItems;
    private T items;
}