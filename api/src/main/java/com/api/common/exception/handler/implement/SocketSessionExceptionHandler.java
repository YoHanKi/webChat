package com.api.common.exception.handler.implement;

import com.api.common.exception.handler.AbstractExceptionHandler;
import com.api.common.exception.model.ApiError;
import com.api.domain.chat.websocket.exception.SocketRoomIDException;
import com.api.domain.chat.websocket.exception.SocketSessionException;
import com.api.domain.chat.websocket.exception.SocketURIException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SocketSessionExceptionHandler extends AbstractExceptionHandler<SocketSessionException> {

    public SocketSessionExceptionHandler() {
        super(List.of(SocketURIException.class, SocketRoomIDException.class));
    }

    @Override
    protected ApiError createError(Exception e) {
    if (e instanceof SocketURIException) {
            return new ApiError(400, "잘못된 소켓 URI입니다.");
        } else if (e instanceof SocketRoomIDException) {
            return new ApiError(400, "잘못된 소켓 방 ID입니다.");
        }
        return new ApiError(500, "알 수 없는 소켓 세션 오류가 발생했습니다.");
    }
}
