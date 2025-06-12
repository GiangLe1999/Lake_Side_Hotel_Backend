package vn.riverlee.lake_side_hotel.service.impl;

import vn.riverlee.lake_side_hotel.dto.request.InitChatRequest;
import vn.riverlee.lake_side_hotel.dto.request.SendMessageRequest;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;
import vn.riverlee.lake_side_hotel.enums.MessageType;
import vn.riverlee.lake_side_hotel.enums.SenderType;
import vn.riverlee.lake_side_hotel.exception.ResourceNotFoundException;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.model.ChatMessage;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.repository.ChatConversationRepository;
import vn.riverlee.lake_side_hotel.repository.ChatMessageRepository;
import vn.riverlee.lake_side_hotel.repository.UserRepository;
import vn.riverlee.lake_side_hotel.util.ChatMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    // Tạo mới 1 ChatConversation
    public ChatConversationResponse initializeChat(InitChatRequest request, Authentication authentication) {
        ChatConversation conversation = ChatConversation.builder()
                // sinh sessionId ngẫu nhiên
                .sessionId(generateSessionId())
                .status(ChatStatus.ACTIVE)
                .roomId(request.getRoomId())
                .lastMessageAt(LocalDateTime.now())
                .build();

        // Nếu user đã login => lấy User gán vào conversation
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            conversation.setUser(user);
        } else {
            // Nếu Guest => lấy name, email từ request
            conversation.setGuestName(request.getGuestName());
            conversation.setGuestEmail(request.getGuestEmail());
        }

        conversation = conversationRepository.save(conversation);

        // Gửi tin nhắn chào mừng
        sendWelcomeMessage(conversation);

        return chatMapper.toDTO(conversation);
    }

    public ChatMessageResponse sendMessage(SendMessageRequest request, Authentication authentication) {
        // Tìm conversation
        ChatConversation conversation = conversationRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat conversation not found"));

        // Xác định loại người gửi
        String senderName = request.getSenderName();
        SenderType senderType = SenderType.USER;

        // Nếu user đã đăng nhập, lấy tên từ user
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            senderName = user.getFullName();

            // Kiểm tra xem đây có phải admin không
            if (user.getRole().name().equals("ADMIN")) {
                senderType = SenderType.ADMIN;
            }
        }

        // Tạo tin nhắn
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .content(request.getContent())
                .senderType(senderType)
                .messageType(request.getMessageType())
                .senderName(senderName)
                .fileUrl(request.getFileUrl())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        // Cập nhật thời gian tin cuối của conversation
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse messageDTO = chatMapper.toDTO(message);

        // Gửi tin nhắn đến các client đang sub WebSocket
        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversation.getSessionId(),
                messageDTO
        );

        // Also send to admin dashboard
        messagingTemplate.convertAndSend("/topic/admin/chat", messageDTO);

        return messageDTO;
    }

    // Tìm conversation theo sessionId (lấy ra cả thông tin tất cả tin nhắn trong conversation)
    @Transactional(readOnly = true)
    public ChatConversationResponse getConversation(String sessionId) {
        ChatConversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat conversation not found"));

        return chatMapper.toDTO(conversation);
    }


    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(String sessionId, int page, int size) {
        ChatConversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat conversation not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository.findByConversationOrderByCreatedAtDesc(
                conversation, pageable);

        return messages.getContent().stream()
                .map(chatMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChatConversationResponse> getActiveConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatConversation> conversations = conversationRepository
                .findActiveConversations(ChatStatus.ACTIVE, pageable);

        return conversations.map(chatMapper::toDTO);
    }

    public void markMessagesAsRead(String sessionId) {
        ChatConversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat conversation not found"));

        List<ChatMessage> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
        messages.forEach(message -> message.setIsRead(true));
        messageRepository.saveAll(messages);
    }

    public void closeConversation(String sessionId) {
        ChatConversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat conversation not found"));

        conversation.setStatus(ChatStatus.CLOSED);
        conversationRepository.save(conversation);

        // Notify clients that conversation is closed
        messagingTemplate.convertAndSend(
                "/topic/chat/" + sessionId,
                ChatMessageResponse.builder()
                        .content("Cuộc trò chuyện đã được đóng.")
                        .senderType(SenderType.SYSTEM)
                        .messageType(MessageType.SYSTEM_MESSAGE)
                        .build()
        );
    }

    private void sendWelcomeMessage(ChatConversation conversation) {
        ChatMessage welcomeMessage = ChatMessage.builder()
                .conversation(conversation)
                .content("Hello! How can we assist you today?")
                .senderType(SenderType.SYSTEM)
                .messageType(MessageType.SYSTEM_MESSAGE)
                .senderName("System")
                .isRead(false)
                .build();

        messageRepository.save(welcomeMessage);
    }

    private String generateSessionId() {
        return "chat_" + UUID.randomUUID().toString().replace("-", "");
    }
}
