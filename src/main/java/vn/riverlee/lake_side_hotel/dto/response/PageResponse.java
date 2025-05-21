package vn.riverlee.lake_side_hotel.dto.response;

public class PageResponse<T> {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private int totalAvailableItems;
    private T items;
}
