package vn.riverlee.lake_side_hotel.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.EditRoomRequest;
import vn.riverlee.lake_side_hotel.dto.request.RoomRequest;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.model.Room;
import vn.riverlee.lake_side_hotel.repository.RoomRepository;
import vn.riverlee.lake_side_hotel.service.RoomService;
import vn.riverlee.lake_side_hotel.service.S3Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final S3Service s3Service;


    @Override
    public long addNewRoom(RoomRequest request) throws IOException {
        // Upload thumbnail
        String thumbnailKey = null;
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            thumbnailKey = s3Service.uploadFile(request.getThumbnail());
        }

        // Upload images
        List<String> imageKeys;
        imageKeys = s3Service.uploadMultipleFiles(request.getImages());

        // Tạo và lưu Room
        Room room = Room.builder()
                .type(request.getType())
                .summary(request.getSummary())
                .description(request.getDescription())
                .area(request.getArea())
                .beds(request.getBeds())
                .amenities(request.getAmenities())
                .features(request.getFeatures())
                .totalRooms(request.getTotalRooms())
                .price(request.getPrice())
                .thumbnailKey(thumbnailKey)
                .imageKeys(imageKeys)
                .build();

        Room savedRoom = roomRepository.save(room);
        return savedRoom.getId();
    }

    @Override
    public List<String> getRoomTypes() {
        return roomRepository.getDistinctRoomTypes();
    }

    @Override
    public PaginationResponse<Object> getRoomsFilteredByRoomType(int pageNo, int pageSize, String roomType) {
        Page<Room> page;
        if (roomType == null || roomType.trim().isEmpty()) {
            page = roomRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("createdAt")));
        } else {
            page = roomRepository.findByType(roomType, PageRequest.of(pageNo, pageSize, Sort.by("createdAt")));
        }

        List<RoomResponse> roomResponse = page.stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .type(room.getType())
                        .price(room.getPrice())
                        .totalRooms(room.getTotalRooms())
                        .createdAt(room.getCreatedAt())
                        .build())
                .toList();

        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(page.getTotalPages())
                .items(roomResponse)
                .build();
    }

    @Override
    public Long deleteRoom(Long id) throws ResourceNotFoundException {
        // 1. Tìm Room theo ID
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + id + " not found"));

        // 2. Xóa thumbnail nếu có
        String thumbnailKey = room.getThumbnailKey();
        if (thumbnailKey != null && !thumbnailKey.isEmpty()) {
            s3Service.deleteFile(thumbnailKey); // Xóa 1 file
        }

        // 3. Xóa tất cả imageKeys nếu có
        List<String> imageKeys = room.getImageKeys();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            s3Service.deleteMultipleFiles(imageKeys); // Xóa nhiều file
        }

        // 4. Xóa room trong DB
        roomRepository.deleteById(id);

        // 5. Trả về ID đã xóa
        return id;
    }

    @Override
    public RoomResponse getRoom(long id) throws ResourceNotFoundException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + id + " not found"));

        return RoomResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .summary(room.getSummary())
                .description(room.getDescription())
                .area(room.getArea())
                .beds(room.getBeds())
                .amenities(room.getAmenities())
                .features(room.getFeatures())
                .totalRooms(room.getTotalRooms())
                .price(room.getPrice())
                .thumbnailKey(room.getThumbnailKey())
                .imageKeys(room.getImageKeys()).build();
    }

    @Override
    public RoomResponse getRoomForAdmin(long id) throws ResourceNotFoundException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + id + " not found"));

        return RoomResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .summary(room.getSummary())
                .description(room.getDescription())
                .area(room.getArea())
                .beds(room.getBeds())
                .amenities(room.getAmenities())
                .features(room.getFeatures())
                .totalRooms(room.getTotalRooms())
                .price(room.getPrice())
                .thumbnailKey(room.getThumbnailKey())
                .imageKeys(room.getImageKeys()).build();
    }

    // @Transactional để đảm bảo rollback nếu có lỗi
    @Override
    @Transactional // Đảm bảo rollback nếu có lỗi
    public Long editRoom(long id, EditRoomRequest request) throws IOException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + id + " not found"));

        // Update type
        if (request.getType() != null) {
            room.setType(request.getType());
        }

        // Update summary
        if (request.getSummary() != null) {
            room.setSummary(request.getSummary());
        }

        // Update description
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }

        // Update area
        if (request.getArea() != null) {
            room.setArea(request.getArea());
        }

        // Update beds
        if (request.getBeds() != null) {
            room.setBeds(request.getBeds());
        }

        // Update amenities
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            room.setAmenities(request.getAmenities());
        }

        // Update features
        if (request.getFeatures() != null && !request.getFeatures().isEmpty()) {
            room.setFeatures(request.getFeatures());
        }

        // Update total rooms
        if (request.getTotalRooms() != null) {
            room.setTotalRooms(request.getTotalRooms());
        }

        // Update price
        if (request.getPrice() != null) {
            room.setPrice(request.getPrice());
        }

        // Update thumbnail nếu có
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            s3Service.deleteFile(room.getThumbnailKey());
            String newThumbnailKey = s3Service.uploadFile(request.getThumbnail());
            room.setThumbnailKey(newThumbnailKey);
        }

        // Update images nếu có
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            s3Service.deleteMultipleFiles(room.getImageKeys());
            List<String> newImageKeys = s3Service.uploadMultipleFiles(request.getImages());
            room.setImageKeys(newImageKeys);
        }

        roomRepository.save(room);
        return room.getId();
    }

    @Override
    public PaginationResponse<Object> getRooms(int pageNo, int pageSize) {
        Page<Room> page = roomRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("avgRating")));

        List<RoomResponse> roomResponse = page.stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .type(room.getType())
                        .summary(room.getSummary())
                        .description(room.getDescription())
                        .area(room.getArea())
                        .beds(room.getBeds())
                        .amenities(room.getAmenities())
                        .features(room.getFeatures())
                        .thumbnailKey(room.getThumbnailKey())
                        .imageKeys(room.getImageKeys())
                        .price(room.getPrice())
                        .createdAt(room.getCreatedAt())
                        .build())
                .toList();

        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(page.getTotalPages())
                .items(roomResponse)
                .build();
    }
}
