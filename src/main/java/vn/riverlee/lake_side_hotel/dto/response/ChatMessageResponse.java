package vn.riverlee.lake_side_hotel.dto.response;

import lombok.*;
import vn.riverlee.lake_side_hotel.enums.MessageType;
import vn.riverlee.lake_side_hotel.enums.SenderType;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private String content;
    private SenderType senderType;
    private MessageType messageType;
    private String senderName;
    private Boolean isRead;
    private String fileUrl;
    private Date createdAt;
    private String conversationSessionId;
}