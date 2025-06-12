package vn.riverlee.lake_side_hotel.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatStatsResponse;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.repository.ChatConversationRepository;
import vn.riverlee.lake_side_hotel.repository.ChatMessageRepository;
import vn.riverlee.lake_side_hotel.service.AdminChatService;
import vn.riverlee.lake_side_hotel.util.ChatMapper;

@Service
@RequiredArgsConstructor
public class AdminChatServiceImpl implements AdminChatService {
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMapper chatMapper;

    @Transactional(readOnly = true)
    public Page<ChatConversationResponse> getAllConversations(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatConversation> conversations;

        if (status != null && !status.isEmpty()) {
            ChatStatus chatStatus = ChatStatus.valueOf(status.toUpperCase());
            conversations = conversationRepository.findByStatusOrderByLastMessageAtDesc(chatStatus, pageable);
        } else {
            conversations = conversationRepository.findAll(pageable);
        }

        return conversations.map(chatMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ChatStatsResponse getChatStats() {
        long activeConversations = conversationRepository.countByStatus(ChatStatus.ACTIVE);
        long closedConversations = conversationRepository.countByStatus(ChatStatus.CLOSED);
        long waitingConversations = conversationRepository.countByStatus(ChatStatus.WAITING_ADMIN);

        return ChatStatsResponse.builder()
                .activeConversations(activeConversations)
                .closedConversations(closedConversations)
                .waitingConversations(waitingConversations)
                .totalConversations(activeConversations + closedConversations + waitingConversations)
                .build();
    }

    public void assignConversation(String sessionId, Long adminId) {
        // Implementation cho việc assign conversation cho admin cụ thể
        // Có thể thêm admin_id field vào ChatConversation entity
    }
}
