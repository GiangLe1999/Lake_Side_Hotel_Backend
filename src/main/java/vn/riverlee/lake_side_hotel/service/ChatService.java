package vn.riverlee.lake_side_hotel.service;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import vn.riverlee.lake_side_hotel.dto.request.InitChatRequest;
import vn.riverlee.lake_side_hotel.dto.request.SendMessageRequest;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;

import java.util.List;

public interface ChatService {
    ChatConversationResponse initializeChat(InitChatRequest request, Authentication authentication);

    ChatMessageResponse sendMessage(SendMessageRequest request, Authentication authentication);

    ChatConversationResponse getConversation(String sessionId);

    List<ChatMessageResponse> getMessages(String sessionId, int page, int size);

    void markMessagesAsRead(String sessionId);

    void closeConversation(String sessionId);

    PaginationResponse<?> getConversations(int pageNo, int pageSize, String search, String sortBy, String status);
}
