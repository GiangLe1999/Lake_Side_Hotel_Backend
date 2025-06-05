package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.EditRoomRequest;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.service.RoomService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/rooms/public")
@RequiredArgsConstructor
public class PublicRoomController {
    private final RoomService roomService;
    // Get all room types
    @GetMapping("/types")
    public DataResponse<List<String>> getRoomTypes() {
        log.info("Get room types");
        List<String> roomTypes = roomService.getRoomTypes();
        return new DataResponse<>(HttpStatus.OK.value(), "Get room types successfully", roomTypes);
    }

    // Filter rooms by type at admin room management
    @GetMapping("/filtered-by-type")
    public DataResponse<PaginationResponse> getRoomsFilteredByRoomType(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                       @Min(3) @RequestParam(defaultValue = "3", required = false) int pageSize,
                                                                       @RequestParam(required = false) String roomType) {
        log.info("Get rooms filtered by room type");
        PaginationResponse roomPaginationResponse = roomService.getRoomsFilteredByRoomType(pageNo, pageSize, roomType);
        return new DataResponse<>(HttpStatus.OK.value(), "Get rooms filtered by room type successfully", roomPaginationResponse);
    }

    // Get rooms with full details and pagination (used at homepage)
    @GetMapping("")
    public DataResponse<PaginationResponse> getRooms(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                         @Min(3) @RequestParam(defaultValue = "9", required = false) int pageSize) {
        log.info("Get rooms with full details and pagination");
        PaginationResponse roomPaginationResponse = roomService.getRooms(pageNo, pageSize);
        return new DataResponse<>(HttpStatus.OK.value(), "Get rooms filtered by room type successfully", roomPaginationResponse);
    }

    // Get room information with full details
    @GetMapping("/{id}")
    public DataResponse<RoomResponse> getRoom(@Min(1) @PathVariable long id) throws ResourceNotFoundException {
        log.info("Get room with ID: {}", id);
        RoomResponse room = roomService.getRoom(id);
        return new DataResponse<>(HttpStatus.OK.value(), "Get room successfully", room);
    }
}
