package vn.riverlee.lake_side_hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.request.InitChatRequest;
import vn.riverlee.lake_side_hotel.dto.request.SendMessageRequest;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.service.ChatService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/init")
    public DataResponse<ChatConversationResponse> initializeChat(
            @Valid @RequestBody InitChatRequest request,
            Authentication authentication) {
        ChatConversationResponse conversation = chatService.initializeChat(request, authentication);
        return new DataResponse<>(HttpStatus.CREATED.value(), "Initialize chat successfully", conversation);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ChatConversationResponse> getConversation(
            @PathVariable String sessionId) {
        ChatConversationResponse conversation = chatService.getConversation(sessionId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<ChatMessageResponse> messages = chatService.getMessages(sessionId, page, size);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{sessionId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable String sessionId) {
        chatService.markMessagesAsRead(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/close")
    public ResponseEntity<Void> closeConversation(@PathVariable String sessionId) {
        chatService.closeConversation(sessionId);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @GetMapping("/admin/conversations")
    public ResponseEntity<Page<ChatConversationResponse>> getActiveConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatConversationResponse> conversations = chatService.getActiveConversations(page, size);
        return ResponseEntity.ok(conversations);
    }

    // @GetMapping, @PostMapping chỉ dành cho HTTP route. Không thể dùng trong WebSocket/STOMP
    // Chỉ @MessageMapping mới có thể giúp lắng nghe các message STOMP gửi đến đích /app/chat/send
    @MessageMapping("/chat/send")
    // Sau khi xử lý xong, gửi message (broadcast) đến các client đang subscribe /topic/chat
    @SendTo("/topic/chat")
    public ChatMessageResponse sendMessage(
            @Valid SendMessageRequest request,
            Authentication authentication) {
        return chatService.sendMessage(request, authentication);
    }
}

