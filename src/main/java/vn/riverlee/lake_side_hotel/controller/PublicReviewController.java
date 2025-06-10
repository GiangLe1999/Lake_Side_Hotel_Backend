package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.service.ReviewService;

@Slf4j
@Validated
@RestController
@RequestMapping("/reviews/public")
@RequiredArgsConstructor
public class PublicReviewController {
    private final ReviewService reviewService;

    @GetMapping("")
    public DataResponse<PaginationResponse> getRoomReviews(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                     @Min(3) @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                     @Min(1) @RequestParam() long roomId) {
        log.info("Get review of room with id: {}", roomId);
        PaginationResponse roomPaginationResponse = reviewService.getRoomReviews(pageNo, pageSize, roomId);
        return new DataResponse<>(HttpStatus.OK.value(), "Get reviews of room successfully", roomPaginationResponse);
    }
}
