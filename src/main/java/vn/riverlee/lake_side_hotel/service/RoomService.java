package vn.riverlee.lake_side_hotel.service;

import jakarta.validation.Valid;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;

import java.io.IOException;

public interface RoomService {
    long addNewRoom(@Valid RoomRequest request) throws IOException;
}
