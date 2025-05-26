package vn.riverlee.lake_side_hotel.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import vn.riverlee.lake_side_hotel.dto.request.EditRoomRequest;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;

import java.io.IOException;
import java.util.List;

public interface RoomService {
    long addNewRoom(@Valid RoomRequest request) throws IOException;

    List<String> getRoomTypes();

    PaginationResponse getRoomsFilteredByRoomType(int pageNo, int pageSize, String roomType);

    Long deleteRoom(@Valid Long id) throws ResourceNotFoundException;

    RoomResponse getRoom(@Min(1) long id);

    Long editRoom(@Min(1) long id, @Valid EditRoomRequest request) throws IOException;
}
