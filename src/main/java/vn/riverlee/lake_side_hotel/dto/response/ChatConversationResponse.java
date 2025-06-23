package vn.riverlee.lake_side_hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatConversationResponse {
    private Long id;
    private String sessionId;
    private String guestName;
    private String guestEmail;
    private String userName; // Tên user nếu đã đăng nhập
    private String userEmail; // Email user nếu đã đăng nhập
    private ChatStatus status;
    private Long roomId;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private List<ChatMessageResponse> messages;
    private Boolean isReadByAdmin;
    private int unreadCount;
}