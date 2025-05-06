package com.api.domain.streaming.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalingWebSocketHandler extends TextWebSocketHandler {
  private final RedisTemplate<String, String> redisStreamingTemplate;
  private final Map<String, Set<WebSocketSession>> rtcSessions = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String roomId = getParam(session, "roomId");
    rtcSessions
      .computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
      .add(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
    // 그대로 같은 방의 다른 세션에 재전송 (간단 브로드캐스트)
    JsonNode node = new ObjectMapper().readTree(msg.getPayload());
    String roomId = node.get("roomId").asText();
    for (WebSocketSession peer : rtcSessions.getOrDefault(roomId, Set.of())) {
      if (!peer.getId().equals(session.getId()) && peer.isOpen()) {
        peer.sendMessage(msg);
      }
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    String roomId = getParam(session, "roomId");
    rtcSessions.getOrDefault(roomId, Set.of()).remove(session);
  }

  private String getParam(WebSocketSession s, String key) {
    return UriComponentsBuilder.fromUri(s.getUri())
      .build().getQueryParams().getFirst(key);
  }
}