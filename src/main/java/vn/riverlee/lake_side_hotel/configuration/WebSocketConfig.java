package vn.riverlee.lake_side_hotel.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
// Bật WebSocket message handling bằng cách sử dụng broker (bộ phát tin nhắn).
// Cho phép ứng dụng sử dụng STOMP (Simple Text Oriented Messaging Protocol) để gửi/nhận tin nhắn qua WebSocket.
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một Simple In-Memory Broker
        // tức là tin nhắn sẽ được lưu và phân phối trong bộ nhớ máy chủ (không cần RabbitMQ hay ActiveMQ)
        // Tin nhắn gửi từ server tới client sẽ có prefix là:
        // topic: dùng cho broadcast message (tin nhắn gửi cho nhiều người cùng lúc, ví dụ: thông báo chung).
        // queue: dùng cho message riêng biệt (point-to-point message, ví dụ: chat riêng).
        config.enableSimpleBroker("/topic", "/queue");
        // Khi client gửi message đến server, prefix này phải có. Ví dụ:
        // stompClient.send("/chat-app/chat", {}, JSON.stringify(message));
        // Server sẽ route những tin nhắn bắt đầu bằng /app đến controller handler
        // (các method dùng annotation như @MessageMapping("/chat")).
        config.setApplicationDestinationPrefixes("/chat-app");
        // Định nghĩa prefix cho các user-specific destinations.
        // Tin nhắn gửi riêng cho user nào đó sẽ dùng prefix /user để phân biệt (ví dụ: /user/{userId}/queue/messages).
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint WebSocket tại URL: /ws — client phải connect đến URL này để bắt đầu phiên WebSocket.
        registry.addEndpoint("/ws")
                // Cho phép tất cả các nguồn (domains) kết nối đến WebSocket server (CORS cho WebSocket).
                .setAllowedOriginPatterns("*")
                // Cho phép sử dụng SockJS nếu trình duyệt không hỗ trợ WebSocket gốc (fallback mechanism)
                .withSockJS();
    }
}

/*
Tóm tắt luồng hoạt động:
Bước	Vai trò	                               Đường dẫn (Prefix)	           Mô tả
1	    Client gửi tới Server	               /chat-app/**	                   Server handle bằng @MessageMapping
2	    Server gửi tới client (broadcast)	   /topic/**	                   Gửi chung cho nhiều client
3	    Server gửi riêng 1 client	           /user/**	                       Gửi cho từng user (riêng biệt)
4	    Kết nối WebSocket	                   /ws	                           Điểm vào WebSocket từ phía client
5	    Fallback SockJS	                       Có	                           Nếu WebSocket không dùng được

 */