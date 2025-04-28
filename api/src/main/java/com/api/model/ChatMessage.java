package com.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    public enum MessageType { CHAT, JOIN, LEAVE }

    private MessageType type;
    private String sender;
    private String content;
    private String roomId;
}