package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.model.Room;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.imageKeys")
    // Native Query: SELECT r.*, rik.image_key FROM tbl_room r LEFT JOIN tbl_room_image_key rik ON r.id = rik.room_id
    // r là alias cho tbl_room
    // rik là alias cho bảng phụ chứa các image keys
    List<Room> findAllWithImageKeys();

    Page<Room> findByType (String roomType, Pageable pageable);

    long countByType(String type);

    void deleteById(Long id);

    List<Room> findTop3ByOrderByReviewCountDesc();

    @Query("SELECT MAX(r.price) FROM Room r")
    BigDecimal findMaxPrice();

    @Query("SELECT MIN(r.price) FROM Room r")
    BigDecimal findMinPrice();

    @Query("SELECT DISTINCT r.type FROM Room r")
    List<String> findDistinctRoomTypes();

    @Query("SELECT DISTINCT r.beds FROM Room r")
    List<String> findDistinctRoomBeds();

    // ✅ Lấy tất cả occupancy duy nhất
    @Query("SELECT DISTINCT r.occupancy FROM Room r")
    List<Integer> findDistinctOccupancyTypes();

    // ❌ amenities là @ElementCollection -> cần JOIN bảng phụ
    @Query("SELECT DISTINCT a FROM Room r JOIN r.amenities a")
    List<String> findDistinctAmenities();

    // ❌ features là @ElementCollection -> cần JOIN bảng phụ
    @Query("SELECT DISTINCT f FROM Room r JOIN r.features f")
    List<String> findDistinctFeatures();
}
