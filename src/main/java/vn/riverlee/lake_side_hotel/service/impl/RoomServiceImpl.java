package vn.riverlee.lake_side_hotel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
    public PaginationResponse<?> getRoomsFilteredByRoomType(int pageNo, int pageSize, String roomType) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt"));

        List<Room> rooms;
        long totalItems;

        if (roomType == null || roomType.trim().isEmpty()) {
            rooms = roomRepository.findAll(pageable).getContent(); // Trả về tất cả phòng
            totalItems = roomRepository.count(); // Đếm tổng tất cả phòng
        } else {
            rooms = roomRepository.findByType(roomType, pageable);
            totalItems = roomRepository.countByType(roomType); // Đếm tổng các phòng theo loại
        }

        List<RoomResponse> roomResponse = rooms.stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .type(room.getType())
                        .price(room.getPrice())
                        .createdAt(room.getCreatedAt())
                        .build())
                .toList();

        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages((int) Math.ceil((double) totalItems / pageSize))
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
}
