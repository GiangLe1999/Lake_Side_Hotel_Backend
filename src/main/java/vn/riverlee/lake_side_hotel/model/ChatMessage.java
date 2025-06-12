package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.*;
import vn.riverlee.lake_side_hotel.enums.MessageType;
import vn.riverlee.lake_side_hotel.enums.SenderType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_chat_message")
public class ChatMessage extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private SenderType senderType; // USER, ADMIN, SYSTEM

    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT; // TEXT, IMAGE, FILE

    @Column(name = "sender_name")
    private String senderName; // Tên người gửi

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "file_url")
    private String fileUrl; // URL file nếu có
}