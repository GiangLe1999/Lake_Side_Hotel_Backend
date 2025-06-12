package vn.riverlee.lake_side_hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import vn.riverlee.lake_side_hotel.dto.request.SendMessageRequest;
import vn.riverlee.lake_side_hotel.dto.response.ChatMessageResponse;
import vn.riverlee.lake_side_hotel.service.ChatService;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/{sessionId}/send")
    @SendTo("/topic/chat/{sessionId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable String sessionId,
            SendMessageRequest request,
            Authentication authentication) {

        request.setSessionId(sessionId);
        return chatService.sendMessage(request, authentication);
    }

    @MessageMapping("/chat/{sessionId}/typing")
    @SendTo("/topic/chat/{sessionId}/typing")
    public String handleTyping(
            @DestinationVariable String sessionId,
            String senderName) {
        return senderName + " đang nhập...";
    }
}