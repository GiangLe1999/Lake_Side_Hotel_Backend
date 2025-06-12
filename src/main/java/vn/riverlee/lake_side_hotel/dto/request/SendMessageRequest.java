package vn.riverlee.lake_side_hotel.dto.request;

import lombok.*;
import vn.riverlee.lake_side_hotel.enums.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    @NotNull
    private String sessionId;

    @NotBlank
    private String content;

    private MessageType messageType = MessageType.TEXT;

    private String senderName; // Cho guest user

    private String fileUrl; // Nếu gửi file
}