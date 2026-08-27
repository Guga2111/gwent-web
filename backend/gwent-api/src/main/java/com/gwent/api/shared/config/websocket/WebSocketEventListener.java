package com.gwent.api.shared.config.websocket;

import com.gwent.api.game.GameSessionService;
import org.springframework.context.event.EventListener;
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
        WebSocketSessionRegistry.UnregisterResult result = registry.unregisterSession(sessionId);
        if (result != null && result.newCount() == 0) {
            String[] parts = result.key().split(":");
            UUID gameId = UUID.fromString(parts[0]);
            String playerEmail = parts[1];
            gameSessionService.scheduleDisconnectForfeit(gameId, playerEmail);
        }
    }
}
