package vn.riverlee.lake_side_hotel.service;

import org.springframework.security.core.Authentication;
import vn.riverlee.lake_side_hotel.dto.request.InitChatRequest;
import vn.riverlee.lake_side_hotel.dto.request.SendMessageRequest;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;

public interface ChatService {
    String initializeChat(InitChatRequest request, Authentication authentication);

    ChatMessageResponse sendMessage(SendMessageRequest request, Authentication authentication);

    ChatConversationResponse getConversation(String sessionId);

    PaginationResponse<Object> getMessages(String sessionId, int pageNo, int pageSize);

    void deleteConversation(String sessionId);

    PaginationResponse<?> getConversations(int pageNo, int pageSize, String search, String sortBy, String status);

    void markConversationAsRead(String sessionId);

    void toggleConversationStatus(String sessionId, String status);
}
