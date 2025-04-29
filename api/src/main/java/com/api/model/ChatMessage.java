package com.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum MessageType { CHAT, JOIN, LEAVE }

    private MessageType type;
    private String sender;
    private String content;
    private String roomId;
}