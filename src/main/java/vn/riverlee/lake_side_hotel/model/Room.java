package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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
    private List<String> amenities;

    @Column(name = "avg_rating")
    private BigDecimal avgRating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "total_rooms")
    private Integer totalRooms;

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
    private List<String> imageKeys;

    // cascade = CascadeType.ALL - khi room bị xóa, toàn bộ lịch sử đặt phòng (bookings) cũng bị xóa theo
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "room")
    private List<Booking> bookings;

    public void addBooking(Booking booking) {
        if (this.bookings == null) {
            this.bookings = new ArrayList<>();
        }
        this.bookings.add(booking);
        booking.setRoom(this);

        String bookingConfirmationCode = RandomStringUtils.randomNumeric(10);
        booking.setConfirmationCode(bookingConfirmationCode);
    }

    // Cập nhật ảnh
    public void addImageKey(String key) {
        if (this.imageKeys == null) {
            this.imageKeys = new ArrayList<>();
        }
        this.imageKeys.add(key);
    }

    // Custom Builder
    public static class RoomBuilder {
        private String type;
        private String summary;
        private String description;
        private BigDecimal area;
        private String beds;
        private List<String> amenities = new ArrayList<>();
        private BigDecimal avgRating = new BigDecimal("0.0");
        private Integer reviewCount = 0;
        private BigDecimal price;
        private Integer totalRooms = 1;
        private String thumbnailKey;
        private List<String> imageKeys = new ArrayList<>();
        private List<Booking> bookings = new ArrayList<>();

        public RoomBuilder type(String type) {
            this.type = type;
            return this;
        }

        public RoomBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public RoomBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RoomBuilder area(BigDecimal area) {
            this.area = area;
            return this;
        }

        public RoomBuilder beds(String beds) {
            this.beds = beds;
            return this;
        }

        public RoomBuilder amenities(List<String> amenities) {
            this.amenities = amenities;
            return this;
        }

        public RoomBuilder addAmenity(String amenity) {
            this.amenities.add(amenity);
            return this;
        }

        public RoomBuilder avgRating(BigDecimal avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        public RoomBuilder reviewCount(Integer reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public RoomBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public RoomBuilder totalRooms(Integer totalRooms) {
            this.totalRooms = totalRooms;
            return this;
        }

        public RoomBuilder thumbnailKey(String thumbnailKey) {
            this.thumbnailKey = thumbnailKey;
            return this;
        }

        public RoomBuilder imageKeys(List<String> imageKeys) {
            this.imageKeys = imageKeys;
            return this;
        }

        public RoomBuilder addImageKey(String imageKey) {
            this.imageKeys.add(imageKey);
            return this;
        }

        public RoomBuilder bookings(List<Booking> bookings) {
            this.bookings = bookings;
            return this;
        }

        public RoomBuilder addBooking(Booking booking) {
            this.bookings.add(booking);
            return this;
        }

        public Room build() {
            Room room = new Room();
            room.setType(this.type);
            room.setSummary(this.summary);
            room.setDescription(this.description);
            room.setArea(this.area);
            room.setBeds(this.beds);
            room.setAmenities(this.amenities);
            room.setAvgRating(this.avgRating);
            room.setReviewCount(this.reviewCount);
            room.setPrice(this.price);
            room.setTotalRooms(this.totalRooms);
            room.setThumbnailKey(this.thumbnailKey);
            room.setImageKeys(this.imageKeys);
            room.setBookings(this.bookings);
            return room;
        }
    }

    public static RoomBuilder builder() {
        return new RoomBuilder();
    }

    /*
    Room room = Room.builder()
        .type("Deluxe")
        .summary("Phòng sang trọng và tiện nghi")
        .description("Có ban công, view hồ tuyệt đẹp, bao gồm bữa sáng")
        .area(new BigDecimal("35.5"))
        .beds("1 Queen Bed")
        .addAmenity("Wi-Fi miễn phí")
        .addAmenity("TV màn hình phẳng")
        .avgRating(new BigDecimal("4.5"))
        .reviewCount(120)
        .price(new BigDecimal("1500000"))
        .thumbnailKey("deluxe-thumb.jpg")
        .addImageKey("deluxe-1.jpg")
        .totalRooms(1)
        .build();
     */
}
