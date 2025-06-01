package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Room")
@Table(name = "tbl_room")
public class Room extends AbstractEntity {

    @Column(name = "type")
    private String type;

    @Column(name = "summary")
    private String summary;

    @Column(name = "description")
    private String description;

    @Column(name = "area")
    private BigDecimal area;

    @Column(name = "beds")
    private String beds;

    @ElementCollection
    @CollectionTable(name = "tbl_room_amenities", joinColumns = @JoinColumn(name = "room_id")
    )
    @Column(name = "amenities")
    private List<String> amenities = new ArrayList<>();

    @Column(name = "avg_rating")
    private BigDecimal avgRating = new BigDecimal("0.0");

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "total_rooms")
    private Integer totalRooms = 1;

    @Column(name = "thumbnail_key")
    private String thumbnailKey;

    // Một Room có nhiều image keys (chuỗi) được lưu trong bảng phụ room_image_keys, ánh xạ bằng room_id
    // @ElementCollection dùng để lưu list các giá trị đơn giản (String, int...)
    // mà không phải tạo 1 entity riêng (như RoomImage chẳng hạn)
    // @CollectionTable	chỉ định tên bảng phụ để lưu các giá trị của list đó.
    @ElementCollection
    @CollectionTable(
            // Tên bảng phụ được tạo ra
            name = "tbl_room_image_key",
            // Cột dùng để liên kết với bảng chính (Room)
            joinColumns = @JoinColumn(name = "room_id")
    )
    // Tên của cột chứa từng phần tử trong bảng phụ
    @Column(name = "image_key")
    private List<String> imageKeys = new ArrayList<>();

    // cascade = CascadeType.ALL - khi room bị xóa, toàn bộ lịch sử đặt phòng (bookings) cũng bị xóa theo
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "room")
    private List<Booking> bookings = new ArrayList<>();
}
