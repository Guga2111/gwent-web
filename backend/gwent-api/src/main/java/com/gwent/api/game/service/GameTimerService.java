package com.gwent.api.game.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class GameTimerService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<UUID, ScheduledFuture<?>> medicTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> leaderTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> scoiataelTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> turnTimers = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> disconnectTimers = new ConcurrentHashMap<>();

    private final Map<UUID, Long> turnDeadlines = new ConcurrentHashMap<>();
    private final Map<UUID, Long> abilityDeadlines = new ConcurrentHashMap<>();

    private final GameBroadcastService broadcastService;

    public GameTimerService (GameBroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    public void scheduleMedicTimeout(UUID gameId, Runnable action) {
        cancelMedicTimer(gameId);
        abilityDeadlines.put(gameId, System.currentTimeMillis() + 30_000);
        medicTimers.put(gameId, scheduler.schedule(action, 30, TimeUnit.SECONDS));
    }

    public void cancelMedicTimer(UUID gameId) {
        ScheduledFuture<?> timer = medicTimers.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    public void scheduleLeaderTimeout(UUID gameId, Runnable action) {
        cancelLeaderTimer(gameId);
        abilityDeadlines.put(gameId, System.currentTimeMillis() + 30_000);
        leaderTimers.put(gameId, scheduler.schedule(action, 30, TimeUnit.SECONDS));
    }

    public void cancelLeaderTimer(UUID gameId) {
        ScheduledFuture<?> timer = leaderTimers.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    public void scheduleScoiataelTimeout(UUID gameId, Runnable action) {
        cancelScoiataelTimer(gameId);
        abilityDeadlines.put(gameId, System.currentTimeMillis() + 30_000);
        scoiataelTimers.put(gameId, scheduler.schedule(action, 30, TimeUnit.SECONDS));
    }

    public void cancelScoiataelTimer(UUID gameId) {
        ScheduledFuture<?> timer = scoiataelTimers.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    public void scheduleMulliganTimeout(UUID gameId, Runnable action) {
        abilityDeadlines.put(gameId, System.currentTimeMillis() + 30_000);
        scheduler.schedule(action, 30, TimeUnit.SECONDS);
    }

    public void scheduleTurnTimer(UUID gameId, Runnable action) {
        cancelTurnTimer(gameId);
        abilityDeadlines.remove(gameId);
        long deadline = System.currentTimeMillis() + 60_000;
        turnDeadlines.put(gameId, deadline);
        turnTimers.put(gameId, scheduler.schedule(action, 60, TimeUnit.SECONDS));
    }

    public void cancelTurnTimer(UUID gameId) {
        ScheduledFuture<?> timer = turnTimers.remove(gameId);
        turnDeadlines.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    public void scheduleDisconnectForfeit(UUID gameId, String playerEmail, Runnable action) {
        String key = gameId + ":" + playerEmail;
        long deadline = System.currentTimeMillis() + 120_000;
        ScheduledFuture<?> future = scheduler.schedule(action, 120, TimeUnit.SECONDS);
        disconnectTimers.put(key, future);
        broadcastService.broadcastPresence(gameId, playerEmail, false, deadline);
    }

    public void cancelDisconnectForfeit(UUID gameId, String playerEmail) {
        String key = gameId + ":" + playerEmail;
        ScheduledFuture<?> disconnect = disconnectTimers.remove(key);
        if (disconnect != null) {
            disconnect.cancel(false);
            broadcastService.broadcastPresence(gameId, playerEmail, true, null);
        }
    }

    public void cancelDisconnectTimersForGame(UUID gameId, String player1Id, String player2Id) {
        cancelDisconnectForfeit(gameId, player1Id);
        cancelDisconnectForfeit(gameId, player2Id);
    }

    public void cancelAllGameTimers(UUID gameId, String player1Id, String player2Id) {
        cancelMedicTimer(gameId);
        cancelLeaderTimer(gameId);
        cancelScoiataelTimer(gameId);
        cancelTurnTimer(gameId);
        cancelDisconnectTimersForGame(gameId, player1Id, player2Id);
    }

    public Long getTurnDeadline(UUID gameId) {
        return turnDeadlines.get(gameId);
    }

    public Long getAbilityDeadline(UUID gameId) {
        return abilityDeadlines.get(gameId);
    }
}
