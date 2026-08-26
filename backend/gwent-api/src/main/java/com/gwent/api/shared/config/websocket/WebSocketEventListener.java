package com.gwent.api.shared.config.websocket;

import com.gwent.api.game.GameSessionService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
public class WebSocketEventListener {
    private final WebSocketSessionRegistry registry;
    private final GameSessionService gameSessionService;

    public WebSocketEventListener (WebSocketSessionRegistry registry, GameSessionService gameSessionService) {
        this.registry = registry;
        this.gameSessionService = gameSessionService;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Integer count = registry.unregisterSession(sessionId);
        if (count == 0) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
            String[] segments = accessor.getDestination().substring("/topic/games/".length()).split("/");
            if (segments.length < 2) throw new RuntimeException();
            UUID gameId = UUID.fromString(segments[0]);
            String playerEmail = segments[1];
            // gameSessionService.scheduleDisconnectForfeit(gameId, playerEmail);
        }
    }


}
