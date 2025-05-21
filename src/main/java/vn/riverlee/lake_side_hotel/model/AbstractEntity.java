package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter
@Setter
// Đánh dấu một class cha không phải là entity riêng biệt,
// nhưng các entity con kế thừa từ nó sẽ thừa hưởng các trường và ánh xạ (mapping) của nó.
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_at")
    // Tự động thiết lập giá trị của trường này khi đối tượng được lưu lần đầu tiên vào cơ sở dữ liệu
    @CreationTimestamp
    // Muốn lưu thời gian chính xác, bao gồm cả phần giờ, phút, giây.
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
