package vn.riverlee.lake_side_hotel.service;

import jakarta.validation.Valid;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;

import java.io.IOException;
import java.util.List;

public interface RoomService {
    long addNewRoom(@Valid RoomRequest request) throws IOException;

    List<String> getRoomTypes();

    PaginationResponse getRoomsFilteredByRoomType(int pageNo, int pageSize, String roomType);
}
