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
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.service.RoomService;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // Get single room for edit at admin
    @GetMapping("/{id}")
    public DataResponse<RoomResponse> getRoomForAdmin(@Min(1) @PathVariable long id) throws ResourceNotFoundException {
        log.info("Get room for admin with ID: {}", id);
        RoomResponse room = roomService.getRoomForAdmin(id);
        return new DataResponse<>(HttpStatus.OK.value(), "Get room for admin successfully", room);
    }

    // Add new room
    // consumes cho Spring biết controller này chỉ nhận các request có Content-Type là multipart/form-data
    // giúp Spring chọn đúng HttpMessageConverter để xử lý request
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataResponse<Long> addNewRoom(@Valid @ModelAttribute RoomRequest request) throws IOException {
        log.info("Add room request, type = {} vs price = {}", request.getType(), request.getPrice());
        Long roomId = roomService.addNewRoom(request);
        return new DataResponse<>(HttpStatus.CREATED.value(), "Create room successfully", roomId);
    }

    // Delete room
    @DeleteMapping("/{id}")
    public DataResponse<Long> deleteRoom(@Min(1) @PathVariable long id) throws ResourceNotFoundException {
        log.info("Delete room with ID: {}", id);
        Long roomId = roomService.deleteRoom(id);
        return new DataResponse<>(HttpStatus.OK.value(), "Delete room successfully", roomId);
    }

    // Edit room
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataResponse<Long> editRoom(@Min(1) @PathVariable long id, @Valid @ModelAttribute EditRoomRequest request) throws ResourceNotFoundException, IOException {
        log.info("Edit room with ID: {}", id);
        Long roomId = roomService.editRoom(id, request);
        return new DataResponse<>(HttpStatus.OK.value(), "Update room successfully", roomId);
    }
}
