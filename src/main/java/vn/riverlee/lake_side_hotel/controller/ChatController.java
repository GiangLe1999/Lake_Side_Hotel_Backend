package vn.riverlee.lake_side_hotel.controller;

import lombok.RequiredArgsConstructor;
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
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
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
    public DataResponse<PaginationResponse<Object>> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        PaginationResponse<Object> messages = chatService.getMessages(sessionId, pageNo, pageSize);
        return new DataResponse<>(HttpStatus.OK.value(), "Get messages of conversation successfully", messages);
    }

    @PutMapping("/admin/{sessionId}/read")
    public DataResponse<?> markConversationAsRead(@PathVariable String sessionId) {
        chatService.markConversationAsRead(sessionId);
        return new DataResponse<>(HttpStatus.OK.value(), "Mark conversation as read successfully");
    }

    @PutMapping("/admin/{sessionId}/status")
    public DataResponse<String> toggleConversationStatus(@PathVariable String sessionId, @RequestParam String status) {
        chatService.toggleConversationStatus(sessionId, status);
        return new DataResponse<>(HttpStatus.OK.value(), "Update conversation status successfully", sessionId);
    }

    @PostMapping("/admin/{sessionId}/delete")
    public DataResponse<?> deleteConversation(@PathVariable String sessionId) {
        chatService.deleteConversation(sessionId);
        return new DataResponse<>(HttpStatus.OK.value(), "Delete conversation successfully");
    }

    // Admin endpoints
    @GetMapping("/admin/conversations")
    public DataResponse<PaginationResponse<?>> getConversations(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String status
            ) {
        PaginationResponse<?> conversationPaginationResponse = chatService.getConversations(pageNo, pageSize, search, sortBy, status);
        return new DataResponse<PaginationResponse<?>>(HttpStatus.OK.value(), "Get conversations successfully", conversationPaginationResponse);
    }

    // @GetMapping, @PostMapping chỉ dành cho HTTP route. Không thể dùng trong WebSocket/STOMP
    // Chỉ @MessageMapping mới có thể giúp lắng nghe các message STOMP gửi đến đích /app/chat/send
    @MessageMapping("/chat/send")
    // Topic/chat là channel đặc biệt được subscribe bởi admin
    // Khi user gửi message, server publish vào đổng thời cả /topic/chat/{sessionId} và topic/chat để admin nhận được
    @SendTo("/topic/chat")
    public ChatMessageResponse sendMessage(
            @Valid SendMessageRequest request,
            Authentication authentication) {
        return chatService.sendMessage(request, authentication);
    }
}

