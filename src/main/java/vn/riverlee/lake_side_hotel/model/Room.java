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

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "is_booked")
    private boolean isBooked = false;

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

        this.setBooked(true);

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
        private BigDecimal price;
        private boolean isBooked = false;
        private String thumbnailKey;
        private List<String> imageKeys = new ArrayList<>();
        private List<Booking> bookings = new ArrayList<>();

        public RoomBuilder type(String type) {
            this.type = type;
            return this;
        }

        public RoomBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public RoomBuilder isBooked(boolean isBooked) {
            this.isBooked = isBooked;
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
            room.setPrice(this.price);
            room.setBooked(this.isBooked);
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
     * Room room = Room.builder()
     *     .type("Deluxe")
     *     .price(new BigDecimal("1500000"))
     *     .thumbnailKey("deluxe-thumb.jpg")
     *     .addImageKey("deluxe-1.jpg")
     *     .addImageKey("deluxe-2.jpg")
     *     .isBooked(false)
     *     .build();
     */
}
