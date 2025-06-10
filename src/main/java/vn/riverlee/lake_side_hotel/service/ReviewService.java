package vn.riverlee.lake_side_hotel.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import vn.riverlee.lake_side_hotel.dto.request.ReviewRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;

import java.io.IOException;

public interface ReviewService {
    long addNewReview(@Valid ReviewRequest request) throws IOException;

    PaginationResponse<Object> getRoomReviews(int pageNo, int pageSize, long roomId);
}
