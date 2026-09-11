package com.gwent.api.game.service;

import com.gwent.api.game.SessionContext;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.engine.domain.Turn;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class GameSessionRegistry {
    private final Set<UUID> disconnectForfeits = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ReentrantLock> gameLocks = new ConcurrentHashMap<>();
    private final Map<UUID, SessionContext> sessions = new ConcurrentHashMap<>();

    public GameSessionRegistry () {}

    public <T> T executeWithLock(UUID gameId, Function<SessionContext, T> action) {
        ReentrantLock lock = gameLocks.computeIfAbsent(gameId, k -> new ReentrantLock());
        lock.lock();
        try {
            SessionContext ctx = sessions.get(gameId);
            if (ctx == null) throw new GameNotFoundException(gameId);
            return action.apply(ctx);
        } finally {
            lock.unlock();
        }
    }

    public void executeWithLockVoid(UUID gameId, Consumer<SessionContext> action) {
        ReentrantLock lock = gameLocks.computeIfAbsent(gameId, k -> new ReentrantLock());
        lock.lock();
        try {
            SessionContext ctx = sessions.get(gameId);
            if (ctx == null) throw new GameNotFoundException(gameId);
            action.accept(ctx);
        } finally {
            lock.unlock();
        }
    }

    public <T> T executeWithInitLock(UUID gameId, Supplier<T> action) {
        ReentrantLock lock = gameLocks.computeIfAbsent(gameId, k -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public void putSession(UUID gameId, SessionContext ctx) {
        sessions.put(gameId, ctx);
    }

    public Optional<SessionContext> getSession(UUID gameId) {
        return Optional.ofNullable(sessions.get(gameId));
    }

    public Turn resolvePlayer(SessionContext ctx, String userId) {
        if (userId.equals(ctx.player1Id())) return Turn.PLAYER_1;
        if (userId.equals(ctx.player2Id())) return Turn.PLAYER_2;
        throw new PlayerNotInGameException(userId);
    }

    public void addDisconnectForfeit(UUID gameId) {
        disconnectForfeits.add(gameId);
    }

    public void removeDisconnectForfeit(UUID gameId) {
        disconnectForfeits.remove(gameId);
    }

    public boolean hasDisconnectForfeit(UUID gameId) {
        return disconnectForfeits.contains(gameId);
    }
}
