package com.api.domain.chat.websocket.exception;

public class NotContainUserDetailException extends SocketSessionException {
    public NotContainUserDetailException(String message) {
        super(message);
    }
}
