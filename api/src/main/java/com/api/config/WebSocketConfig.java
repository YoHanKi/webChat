package com.api.config;

import com.api.domain.chat.websocket.handler.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;

    public WebSocketConfig(ChatWebSocketHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(chatHandler, "/ws-chat")      // 클라이언트가 접속할 엔드포인트
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("http://localhost:3000");           // CORS 허용
    }
}