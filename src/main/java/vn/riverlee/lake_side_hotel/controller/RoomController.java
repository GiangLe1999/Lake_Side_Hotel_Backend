package vn.riverlee.lake_side_hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.service.RoomService;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    //     consumes cho Spring biết controller này chỉ nhận các request có Content-Type là multipart/form-data
    //     giúp Spring chọn đúng HttpMessageConverter để xử lý request
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add new room", description = "Add new room with multipart/form-data")
    public DataResponse<Long> addNewRoom(@Valid @ModelAttribute RoomRequest request) throws IOException {
        log.info("Add room request, type = {} vs price = {}", request.getType(), request.getPrice());
        long roomId = roomService.addNewRoom(request);
        return new DataResponse<>(HttpStatus.CREATED.value(), "Create room successfully", roomId);
    }
}
