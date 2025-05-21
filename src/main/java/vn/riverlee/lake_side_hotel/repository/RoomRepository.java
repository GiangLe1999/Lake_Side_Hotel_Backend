package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

}
