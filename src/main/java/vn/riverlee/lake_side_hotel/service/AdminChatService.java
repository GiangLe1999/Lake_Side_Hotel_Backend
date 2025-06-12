package vn.riverlee.lake_side_hotel.service;

import org.springframework.data.domain.Page;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatStatsResponse;

public interface AdminChatService {
    Page<ChatConversationResponse> getAllConversations(int page, int size, String status);

    ChatStatsResponse getChatStats();

    void assignConversation(String sessionId, Long adminId);
}
