package com.gwent.api.matchmaking;

import com.gwent.api.game.GameSessionService;
import com.gwent.api.game.dto.CreateGameDto;
import com.gwent.api.matchmaking.dto.MatchFoundDto;
import com.gwent.api.matchmaking.dto.MatchmakingTimeoutDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class MatchmakingService {

    private final Queue<MatchmakingEntry> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, MatchmakingEntry> queueIndex = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final GameSessionService gameSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchmakingService(@Lazy GameSessionService gameSessionService, SimpMessagingTemplate messagingTemplate) {
        this.gameSessionService = gameSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Returns the matched gameId when a match is formed immediately (Player 2 / joiner),
     * or empty when the player is placed in the queue (Player 1 / waiter).
     * Throws {@link AlreadyInQueueException} if the player is already queued.
     */
    public synchronized Optional<UUID> joinQueue(String playerEmail, UUID deckId) {
        if (queueIndex.containsKey(playerEmail)) throw new AlreadyInQueueException();

        MatchmakingEntry opponent = queue.poll();
        if (opponent != null) {
            queueIndex.remove(opponent.playerEmail());
            cancelTimeout(opponent.playerEmail());

            CreateGameDto game = gameSessionService.createSession(opponent.playerEmail(), opponent.deckId());
            gameSessionService.joinSession(game.gameId(), playerEmail, deckId);

            // Only notify the waiting player via WebSocket — they are already subscribed.
            // The joining player receives the gameId directly in the HTTP response.
            messagingTemplate.convertAndSend("/topic/matchmaking/" + opponent.playerEmail(), new MatchFoundDto(game.gameId()));

            return Optional.of(game.gameId());
        }

        MatchmakingEntry entry = new MatchmakingEntry(playerEmail, deckId);
        queue.offer(entry);
        queueIndex.put(playerEmail, entry);
        scheduleTimeout(playerEmail);
        return Optional.empty();
    }

    public synchronized boolean leaveQueue(String playerEmail) {
        MatchmakingEntry entry = queueIndex.remove(playerEmail);
        if (entry == null) return false;
        queue.remove(entry);
        cancelTimeout(playerEmail);
        return true;
    }

    private void scheduleTimeout(String playerEmail) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            synchronized (this) {
                MatchmakingEntry entry = queueIndex.remove(playerEmail);
                if (entry != null) {
                    queue.remove(entry);
                }
            }
            messagingTemplate.convertAndSend("/topic/matchmaking/" + playerEmail, new MatchmakingTimeoutDto());
        }, 120, TimeUnit.SECONDS);
        timeoutFutures.put(playerEmail, future);
    }

    private void cancelTimeout(String playerEmail) {
        ScheduledFuture<?> future = timeoutFutures.remove(playerEmail);
        if (future != null) future.cancel(false);
    }
}
