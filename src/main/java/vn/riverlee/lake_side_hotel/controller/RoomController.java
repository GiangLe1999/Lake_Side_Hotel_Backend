package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.service.RoomService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // consumes cho Spring biết controller này chỉ nhận các request có Content-Type là multipart/form-data
    // giúp Spring chọn đúng HttpMessageConverter để xử lý request
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataResponse<Long> addNewRoom(@Valid @ModelAttribute RoomRequest request) throws IOException {
        log.info("Add room request, type = {} vs price = {}", request.getType(), request.getPrice());
        long roomId = roomService.addNewRoom(request);
        return new DataResponse<>(HttpStatus.CREATED.value(), "Create room successfully", roomId);
    }

    @GetMapping(value = "/types")
    public DataResponse<List<String>> getRoomTypes() {
        log.info("Get room types");
        List<String> roomTypes = roomService.getRoomTypes();
        return new DataResponse<>(HttpStatus.OK.value(), "Get room types successfully", roomTypes);
    }

    @GetMapping(value = "/filtered-by-type")
    public DataResponse<PaginationResponse> getRoomsFilteredByRoomType(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                       @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                       @RequestParam(required = false) String roomType) {
        log.info("Get rooms filtered by room type");
        PaginationResponse roomPaginationResponse = roomService.getRoomsFilteredByRoomType(pageNo, pageSize, roomType);
        return new DataResponse<>(HttpStatus.OK.value(), "Get rooms filtered by room type successfully", roomPaginationResponse);
    }
}
