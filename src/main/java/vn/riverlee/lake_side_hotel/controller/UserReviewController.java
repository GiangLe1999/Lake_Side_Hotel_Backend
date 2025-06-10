package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.ReviewRequest;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.service.ReviewService;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/reviews/user")
@RequiredArgsConstructor
public class UserReviewController {
    private final ReviewService reviewService;

    @PostMapping("")
    public DataResponse<Long> getRoomTypes(@Valid @RequestBody ReviewRequest request) throws IOException {
        log.info("Create review for room with id: {}", request.getRoomId());
        Long id = reviewService.addNewReview(request);
        return new DataResponse<>(HttpStatus.CREATED.value(), "Create review successfully", id);
    }


}
