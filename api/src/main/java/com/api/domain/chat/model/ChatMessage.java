package com.api.domain.chat.model;

import com.api.domain.user.model.RequestReadUserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage implements Serializable {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Serial
    private static final long serialVersionUID = 1L;

    public enum MessageType { CHAT, JOIN, LEAVE, KICK }

    private MessageType type;
    private String sender;
    private String content;
    private String roomId;

    // 특정 상황에만 리스트를 반환
    @Setter
    private List<RequestReadUserDTO> currentUserList;

    public ChatMessage(MessageType type, String sender, String content, String roomId) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.roomId = roomId;
    }

    /**
     * 이 ChatMessage 객체를 JSON 문자열로 변환합니다.
     */
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ChatMessage toJson 실패", e);
        }
    }

    /**
     * 전달된 JSON 문자열을 ChatMessage 객체로 파싱합니다.
     */
    public static ChatMessage fromJson(String json) {
        try {
            // 입력이 따옴표로 감싸진 JSON 문자열인지 확인
            if (json.startsWith("\"") && json.endsWith("\"")) {
                // 먼저 문자열 내의 이스케이프된 JSON을 추출
                String unescapedJson = mapper.readValue(json, String.class);
                // 추출된 실제 JSON을 ChatMessage 객체로 변환
                return mapper.readValue(unescapedJson, ChatMessage.class);
            } else {
                // 일반적인 JSON 직접 변환
                return mapper.readValue(json, ChatMessage.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("ChatMessage fromJson 실패: " + json, e);
        }
    }
}