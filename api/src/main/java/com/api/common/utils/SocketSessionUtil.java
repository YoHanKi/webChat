package com.api.common.utils;

import com.api.domain.chat.websocket.exception.NotContainUserDetailException;
import com.api.domain.chat.websocket.exception.SocketRoomIDException;
import com.api.domain.chat.websocket.exception.SocketURIException;
import com.api.security.model.CustomUserDetails;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

public class SocketSessionUtil {

    /**
     * WebSocketSession의 URI 쿼리 파라미터에서 roomId 값을 꺼내 리턴.
     */
    public static String getRoomIdFromHandshake(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            throw new SocketURIException("웹 소켓 URI가 유효하지 않습니다: " + session.getUri());
        }
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "roomId".equals(kv[0])) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        throw new SocketRoomIDException("roomId parameter is missing in WebSocket URI");
    }

    public static CustomUserDetails getUserDetailsFromSession(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        throw new NotContainUserDetailException("WebSocket session does not contain valid user details.");
    }
}
