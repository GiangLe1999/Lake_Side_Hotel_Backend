package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.*;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_chat_conversation")
public class ChatConversation extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Null cho guest user

    @Column(name = "guest_name")
    private String guestName; // Tên của guest nếu không đăng nhập

    @Column(name = "guest_email")
    private String guestEmail; // Email của guest (optional)

    // Không nên dùng conversationId vì nó là primary key DB nội bộ, frontend không nên biết → tránh lộ thông tin DB
    // Thay vào đó là sử dụng sessionId
    @Column(name = "session_id", unique = true)
    private String sessionId; // Unique session ID

    @Enumerated(EnumType.STRING)
    private ChatStatus status = ChatStatus.ACTIVE;

    @Column(name = "room_id")
    private Long roomId; // ID phòng nếu chat từ room detail

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "conversation")
    private List<ChatMessage> messages;

    @PrePersist
    @PreUpdate
    private void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }
}