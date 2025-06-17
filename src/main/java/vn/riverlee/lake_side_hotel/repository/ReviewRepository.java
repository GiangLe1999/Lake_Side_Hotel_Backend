package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.model.Review;
import vn.riverlee.lake_side_hotel.model.Room;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRoom(Room room, Pageable pageable);
}
