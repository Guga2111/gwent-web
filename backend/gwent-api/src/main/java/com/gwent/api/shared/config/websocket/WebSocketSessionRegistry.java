package com.gwent.api.shared.config.websocket;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionRegistry {
    private final Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToKey = new ConcurrentHashMap<>();

    public record UnregisterResult(String key, int newCount) {}

    public Integer registerSession (String sessionId, UUID gameId, String playerEmail) {
        String key = gameId.toString() + ":" + playerEmail;
        sessionToKey.put(sessionId, key);
        return connectionCounts.merge(key, 1, Integer::sum);
    }

    public UnregisterResult unregisterSession(String sessionId) {
        String key = sessionToKey.remove(sessionId);
        if (key == null) {
            return null;
        }
        int[] result = {-1};
        connectionCounts.compute(key, (k, count) -> {
            if (count == null || count <= 1) { result[0] = 0; return null; }
            result[0] = count - 1;
            return count - 1;
        });
        return new UnregisterResult(key, result[0]);
    }

    public boolean isPlayerConnected (UUID gameId, String playerEmail) {
        String key = gameId.toString() + ":" + playerEmail;
        return connectionCounts.containsKey(key);
    }
}
