package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.riverlee.lake_side_hotel.dto.request.ReviewRequest;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.service.UserReviewService;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/reviews/user")
@RequiredArgsConstructor
public class UserReviewController {
    private final UserReviewService userReviewService;

    @GetMapping("")
    public DataResponse<Long> getRoomTypes(@Valid ReviewRequest request) throws IOException {
        log.info("Create review for room with id: {}", request.getRoomId());
        Long id = userReviewService.addNewReview(request);
        return new DataResponse<>(HttpStatus.CREATED.value(), "Create review successfully", id);
    }
}
