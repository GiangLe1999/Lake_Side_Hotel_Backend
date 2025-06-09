package vn.riverlee.lake_side_hotel.service;

import jakarta.validation.Valid;
import vn.riverlee.lake_side_hotel.dto.request.ReviewRequest;
import java.io.IOException;

public interface UserReviewService {
    long addNewReview(@Valid ReviewRequest request) throws IOException;
}
