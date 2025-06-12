package vn.riverlee.lake_side_hotel.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // Allow anonymous access to chat endpoints
                .simpDestMatchers("/app/chat/**").permitAll()
                .simpSubscribeDestMatchers("/topic/chat/**").permitAll()
                // Admin endpoints require ADMIN role
                .simpDestMatchers("/app/admin/**").hasRole("ADMIN")
                .simpSubscribeDestMatchers("/topic/admin/**").hasRole("ADMIN")
                // Allow connection
                .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true; // Allow cross-origin for development
    }
}