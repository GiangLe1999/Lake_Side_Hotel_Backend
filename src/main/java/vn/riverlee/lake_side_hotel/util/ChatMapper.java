package vn.riverlee.lake_side_hotel.util;

import org.springframework.stereotype.Component;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.model.ChatMessage;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMapper {

    public ChatMessageResponse toDTO(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderType(message.getSenderType())
                .messageType(message.getMessageType())
                .senderName(message.getSenderName())
                .isRead(message.getIsRead())
                .fileUrl(message.getFileUrl())
                .createdAt(message.getCreatedAt())
                .conversationSessionId(message.getConversation().getSessionId())
                .build();
    }

    public ChatConversationResponse toDTO(ChatConversation conversation) {
        List<ChatMessageResponse> messages = null;
        if (conversation.getMessages() != null) {
            messages = conversation.getMessages().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }

        String userName = null;
        String userEmail = null;
        if (conversation.getUser() != null) {
            userName = conversation.getUser().getFullName();
            userEmail = conversation.getUser().getEmail();
        }

        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .sessionId(conversation.getSessionId())
                .guestName(conversation.getGuestName())
                .guestEmail(conversation.getGuestEmail())
                .userName(userName)
                .userEmail(userEmail)
                .status(conversation.getStatus())
                .roomId(conversation.getRoomId())
                .lastMessageAt(conversation.getLastMessageAt())
                .messages(messages)
                .build();
    }
}