package vn.riverlee.lake_side_hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.ChatStatsResponse;
import vn.riverlee.lake_side_hotel.service.AdminChatService;
import vn.riverlee.lake_side_hotel.service.ChatService;

@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminChatController {

    private final ChatService chatService;
    private final AdminChatService adminChatService;

    @GetMapping("/conversations")
    public ResponseEntity<Page<ChatConversationResponse>> getAllConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Page<ChatConversationResponse> conversations = adminChatService.getAllConversations(page, size, status);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/stats")
    public ResponseEntity<ChatStatsResponse> getChatStats() {
        ChatStatsResponse stats = adminChatService.getChatStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/conversations/{sessionId}/assign")
    public ResponseEntity<Void> assignConversation(
            @PathVariable String sessionId,
            @RequestParam Long adminId) {
        adminChatService.assignConversation(sessionId, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversations/{sessionId}/close")
    public ResponseEntity<Void> closeConversation(@PathVariable String sessionId) {
        chatService.closeConversation(sessionId);
        return ResponseEntity.ok().build();
    }
}