package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.model.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT r.type FROM Room r")
    List<String> getDistinctRoomTypes();

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.imageKeys")
    // Native Query: SELECT r.*, rik.image_key FROM tbl_room r LEFT JOIN tbl_room_image_key rik ON r.id = rik.room_id
    // r là alias cho tbl_room
    // rik là alias cho bảng phụ chứa các image keys
    List<Room> findAllWithImageKeys();

    List<Room> findByType (String roomType, Pageable pageable);

    long countByType(String type);

    void deleteById(Long id);
}
