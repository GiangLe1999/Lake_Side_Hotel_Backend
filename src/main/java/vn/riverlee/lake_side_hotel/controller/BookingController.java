package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.BookingRequest;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.BookingResponse;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.service.BookingService;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

//    @GetMapping("")
//    public DataResponse<BookingResponse> getBookings() {
//        log.info("Get all bookings.");
//        return new DataResponse<>(HttpStatus.OK.value(), "Get all bookings successfully");
//    }
//
//    @GetMapping("")
//    public DataResponse<BookingResponse> getBookingByConfirmationCode(@RequestParam() String confirmationCode) {
//        log.info("Get booking by confirmation code.");
//        return new DataResponse<>(HttpStatus.OK.value(), "Get booking by confirmation code successfully");
//    }
//
//    @PostMapping(value = "")
//    public DataResponse<Long> addBooking(@Valid @RequestBody BookingRequest request) {
//        log.info("Add new booking.");
//        return new DataResponse<>(HttpStatus.CREATED.value(), "Add new booking successfully");
//    }
//
//    @DeleteMapping(value = "/{id}")
//    public DataResponse<Long> deleteBooking(@Min(1) @PathVariable long id) {
//        log.info("Delete booking.");
//        return new DataResponse<>(HttpStatus.CREATED.value(), "Delete booking successfully");
//    }
}
