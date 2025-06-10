package vn.riverlee.lake_side_hotel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.ReviewRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ReviewResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.dto.response.UserInfoResponse;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.model.Review;
import vn.riverlee.lake_side_hotel.model.Room;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.repository.ReviewRepository;
import vn.riverlee.lake_side_hotel.repository.RoomRepository;
import vn.riverlee.lake_side_hotel.service.ReviewService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public long addNewReview(ReviewRequest request) throws IOException {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        User user = (User) principal;

        // Tạo review mới
        Review review = Review.builder()
                .room(room)
                .title(request.getTitle())
                .comment(request.getComment())
                .rating(request.getRating())
                .user(user)
                .build();

        // Lưu review
        Review savedReview = reviewRepository.save(review);

        // Cập nhật lại avgRating và reviewCount của room
        int newReviewCount = room.getReviewCount() + 1;
        BigDecimal currentAvg = room.getAvgRating();
        BigDecimal newAvg = currentAvg.multiply(BigDecimal.valueOf(room.getReviewCount()))
                .add(BigDecimal.valueOf(request.getRating()))
                .divide(BigDecimal.valueOf(newReviewCount), 2, RoundingMode.HALF_UP);

        room.setReviewCount(newReviewCount);
        room.setAvgRating(newAvg);
        roomRepository.save(room);

        return savedReview.getId();
    }

    @Override
    public PaginationResponse<Object> getRoomReviews(int pageNo, int pageSize, long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Page<Review> page = reviewRepository.findByRoom(room, PageRequest.of(pageNo, pageSize, Sort.by("createdAt")));

        List<ReviewResponse> reviewResponse = page.stream()
                .map(review ->  {
                    User user = review.getUser();
                    UserInfoResponse userInfo = UserInfoResponse.builder()
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .build();

                    return ReviewResponse.builder()
                            .id(review.getId())
                            .title(review.getTitle())
                            .comment(review.getComment())
                            .rating(review.getRating())
                            .createdAt(review.getCreatedAt())
                            .user(userInfo)
                            .build();
                })
                .toList();

        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(page.getTotalPages())
                .items(reviewResponse)
                .build();
    }
}
