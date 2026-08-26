package com.gwent.api.shared.config.websocket;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionRegistry {
    private final Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToKey = new ConcurrentHashMap<>();

    public Integer registerSession (String sessionId, UUID gameId, String playerEmail) {
        String key = gameId.toString() + ":" + playerEmail;
        sessionToKey.put(sessionId, key);
        return connectionCounts.merge(key, 1, Integer::sum);
    }

    public Integer unregisterSession(String sessionId) {
        String key = sessionToKey.remove(sessionId);
        if (key == null) {
            return null;
        }
        return connectionCounts.computeIfPresent(key, (k, count) -> count > 1 ? count - 1 : null);
    }

    public boolean isPlayerConnected (UUID gameId, String playerEmail) {
        String key = gameId.toString() + ":" + playerEmail;
        return connectionCounts.containsKey(key);
    }
}
