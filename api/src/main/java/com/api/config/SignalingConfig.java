package com.api.config;

import com.api.domain.streaming.websocket.SignalingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class SignalingConfig implements WebSocketConfigurer {
    private final SignalingWebSocketHandler signalingHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
          .addHandler(signalingHandler, "/ws-signal")
          .addInterceptors(new HttpSessionHandshakeInterceptor())
          .setAllowedOriginPatterns("http://localhost:3000");
    }
}