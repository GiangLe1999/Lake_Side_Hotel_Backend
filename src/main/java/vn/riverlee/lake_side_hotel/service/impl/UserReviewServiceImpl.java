package vn.riverlee.lake_side_hotel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.ReviewRequest;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.model.Review;
import vn.riverlee.lake_side_hotel.model.Room;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.repository.ReviewRepository;
import vn.riverlee.lake_side_hotel.repository.RoomRepository;
import vn.riverlee.lake_side_hotel.service.UserReviewService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserReviewServiceImpl implements UserReviewService {
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
}
