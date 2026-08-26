# Epic: Turn Timer & Disconnection Handling

## Context

Currently players have infinite time to play on their turn and if a player disconnects the game hangs forever. This epic adds:

- **Turn timer**: 60s per turn, auto-pass on expiry, visible countdown in the last 10 seconds, subtle progress bar always visible
- **Disconnect handling**: 2-minute reconnection window, auto-forfeit on expiry, opponent notified via presence topic

## Decisions Log

| Topic | Decision | Rationale |
|-------|----------|-----------|
| Turn timer on pending ability | Reset to fresh 60s after ability resolves | Simplicity for demo |
| Turn timer on turn switch | Reset to 60s every time | Consistent behavior |
| Session tracking | Reference counting per (gameId, playerEmail) | Handles multi-tab without false disconnects |
| Disconnect forfeit | Reuse existing `SURRENDER` EndReason | Avoid engine changes, good enough for demo |
| Connection status del3ivery | Separate `/topic/games/{gameId}/presence` topic | SRP — connection status is not game state |
| Countdown UI | Progress bar always visible + large numbers at 10s | Better UX awareness |

## Architecture Notes

### Existing timer infrastructure (reuse this pattern)

`GameSessionService` already has:
- `ScheduledExecutorService scheduler` (4 threads) at line 43
- `Map<UUID, ScheduledFuture<?>>` per timer type (medic, leader, scoiatael)
- Schedule/cancel helper methods per timer
- All timers acquire the game lock, verify state, execute command, broadcast, and persist

### Key files you'll touch

**Backend:**
- `backend/gwent-api/src/main/java/com/gwent/api/game/GameSessionService.java` — timer scheduling, turn timer, disconnect timer
- `backend/gwent-api/src/main/java/com/gwent/api/shared/config/websocket/WebsocketChannelInterceptor.java` — session registration on SUBSCRIBE
- `backend/gwent-api/src/main/java/com/gwent/api/game/GameModelMapper.java` — add `turnDeadlineUtc` to DTO mapping
- `backend/gwent-api/src/main/java/com/gwent/api/game/dto/GameStateDto.java` — add `turnDeadlineUtc` field

**New backend files:**
- `WebSocketSessionRegistry.java` — tracks (gameId, playerEmail) connection counts
- `WebSocketEventListener.java` — listens for `SessionDisconnectEvent`, triggers forfeit timer

**Frontend:**
- `frontend/src/types/game.ts` — add `turnDeadlineUtc` to GameStateDto interface
- `frontend/src/hooks/useWebSocket.ts` — subscribe to presence topic
- `frontend/src/pages/Game.tsx` — mount new components

**New frontend files:**
- `TurnCountdown.tsx` — progress bar + 10s countdown overlay
- `DisconnectedBanner.tsx` — opponent disconnected notification

---

## Slice 1 — Backend: Turn Timer Infrastructure

**Goal:** Auto-pass after 60s of inactivity during PLAY phase.

**Depends on:** Nothing (standalone)

### Tasks

- [ ] **1.1** Add `Map<UUID, ScheduledFuture<?>> turnTimers` to `GameSessionService`

- [ ] **1.2** Add `Map<UUID, Long> turnDeadlines` to store the deadline timestamp (epoch millis) when each timer was scheduled. You'll need this in Slice 2 for the DTO.

- [ ] **1.3** Create `scheduleTurnTimer(UUID gameId)` method:
  - Compute deadline: `System.currentTimeMillis() + 60_000`
  - Store in `turnDeadlines`
  - Cancel any existing turn timer for this game first
  - Schedule a 60s task on `scheduler` that:
    1. Acquires the game lock (`gameLocks`)
    2. Gets the `SessionContext`
    3. Guards: phase must still be `PLAY`, no pending ability, game not over
    4. Gets `currentTurn` from game state
    5. Executes `PassCommand` via `engine.execute(state, new PassCommand())`
    6. Calls `broadcastState()`
    7. Calls `persist()`
    8. Schedules the **next** turn timer if the game continues (turn switched to opponent)

- [ ] **1.4** Create `cancelTurnTimer(UUID gameId)` method:
  - Cancel the scheduled future
  - Remove from `turnTimers` and `turnDeadlines`

- [ ] **1.5** Call `scheduleTurnTimer` at these points:
  - After `engine.startPlay(state)` is called (mulligan phase ends, first turn begins) — look for where `startPlay` is called in `GameSessionService`
  - After every `execute()` call in the main `execute` method — but **only if** the turn actually switched. Compare `currentTurn` before and after execution. If it changed and phase is still `PLAY` and no pending ability, schedule the timer
  - After Scoiatael resolution leads to mulligan, which leads to startPlay — trace this flow

- [ ] **1.6** Call `cancelTurnTimer` at these points:
  - When a pending ability activates (MEDIC_CHOICE, LEADER_*, etc.) — the existing 30s timers handle those
  - When the game ends (GAME_OVER phase)
  - When `surrender()` is called
  - When phase transitions to ROUND_END

- [ ] **1.7** When a pending ability resolves and no new pending ability is set, schedule a fresh 60s turn timer (the turn resumes)

- [ ] **1.8** Handle the edge case: auto-pass might end the round (both players passed). If after auto-pass the phase becomes ROUND_END or GAME_OVER, do NOT schedule a new turn timer.

- [ ] **1.9** Handle the edge case: if the auto-passed player's opponent has already passed, the round ends. The `PassCommand` execution already handles this in the engine — just make sure the timer task checks the resulting phase before scheduling the next timer.

### Verification

- Start a game, reach PLAY phase, do nothing for 60s → your turn should auto-pass
- Play a card → timer resets (opponent now has 60s)
- Trigger MEDIC ability → turn timer cancelled, 30s medic timer active → resolve medic → fresh 60s turn timer starts
- Both players pass (round ends) → no turn timer during ROUND_END → new round starts → timer starts for first player

---

## Slice 2 — Backend: Turn Deadline in GameStateDto

**Goal:** Frontend can show synchronized countdown without clock drift.

**Depends on:** Slice 1

### Tasks

- [ ] **2.1** Add `Long turnDeadlineUtc` field to `GameStateDto` (can be null)

- [ ] **2.2** In `GameModelMapper`, when building `GameStateDto`, read from `turnDeadlines.get(gameId)` and include it. You'll need to pass the deadline value to the mapper somehow — either via a parameter or by making the mapper aware of the deadlines map.

- [ ] **2.3** Set `turnDeadlineUtc = null` when:
  - Phase is not PLAY
  - A pending ability is active
  - Game is over

- [ ] **2.4** Add `turnDeadlineUtc: number | null` to the frontend `GameStateDto` interface in `frontend/src/types/game.ts`

### Verification

- Open browser dev tools, inspect WebSocket messages
- During PLAY phase: `turnDeadlineUtc` should be a future epoch millis timestamp
- During REDRAW/ROUND_END/GAME_OVER: `turnDeadlineUtc` should be null
- During pending ability: `turnDeadlineUtc` should be null

---

## Slice 3 — Frontend: Turn Countdown UI

**Goal:** Progress bar always visible during turn + dramatic 10-second countdown overlay.

**Depends on:** Slice 2

### Tasks

- [ ] **3.1** Create `TurnCountdown` component at `frontend/src/components/board/overlays/TurnCountdown.tsx`:
  - Props or reads from gameStore: `turnDeadlineUtc`, `isMyTurn`
  - Uses `useState` + `setInterval` (every 1s or 100ms for smooth progress bar) to compute remaining seconds from `turnDeadlineUtc - Date.now()`
  - Clean up interval on unmount and when `turnDeadlineUtc` changes

- [ ] **3.2** Implement the **progress bar** portion:
  - Thin bar at top of the board (or along the central divider area — your call on placement)
  - Width = `(remainingMs / 60000) * 100%`
  - Color transitions: gold when > 10s, red when <= 10s
  - Only visible when `turnDeadlineUtc` is not null
  - Show for both players (your turn and opponent's turn), maybe different opacity or color

- [ ] **3.3** Implement the **countdown numbers** portion:
  - Only renders when `remaining <= 10` AND `isMyTurn`
  - Large centered number (10, 9, 8... 1) — similar positioning to other overlays but **not blocking interaction** (use `pointer-events: none`)
  - Style with the medieval theme: use `--font-heading` (Cinzel), `--gold-light` color
  - Add pulse/scale animation on each number change
  - Semi-transparent so the board is still visible behind it

- [ ] **3.4** Mount `TurnCountdown` in `Game.tsx`:
  - Render it alongside (not inside) other overlays
  - Pass `isMyTurn` and the deadline from game state
  - Should be visible even when other overlays are not showing

- [ ] **3.5** Add CSS styles in `frontend/src/styles/index.css`:
  - Progress bar styling with smooth width transition
  - Countdown number animation (pulse/fade)
  - Color transition from gold to red
  - `pointer-events: none` on the countdown numbers so they don't block card clicks

### Verification

- Enter PLAY phase → progress bar appears, slowly decreasing
- Wait until 10s remain → large countdown numbers appear on screen
- Play a card (turn switches) → progress bar resets to full, countdown disappears
- Opponent's turn → progress bar shows opponent's remaining time (maybe different color/opacity)
- Pending ability active → progress bar hidden (turnDeadlineUtc is null)

---

## Slice 4 — Backend: WebSocket Session Tracking

**Goal:** Know which players are connected to which games.

**Depends on:** Nothing (standalone, parallel with Slices 1-3)

### Tasks

- [ ] **4.1** Create `WebSocketSessionRegistry` service (`@Service`) in the websocket config package or game package:
  ```
  Fields:
  - ConcurrentHashMap<String, Integer> connectionCounts
    Key format: "gameId:playerEmail"
    Value: number of active sessions

  - ConcurrentHashMap<String, String> sessionToKey
    Maps STOMP sessionId -> "gameId:playerEmail" key
  ```

- [ ] **4.2** Add `registerSession(String sessionId, UUID gameId, String playerEmail)` method:
  - Store sessionId -> key mapping
  - Atomically increment the connection count for that key
  - Return the new count

- [ ] **4.3** Add `unregisterSession(String sessionId)` method:
  - Look up the key from sessionToKey
  - Remove the session mapping
  - Atomically decrement the connection count
  - If count reaches 0, remove the entry
  - Return the new count (0 means fully disconnected)

- [ ] **4.4** Add `isPlayerConnected(UUID gameId, String playerEmail)` method:
  - Returns `connectionCounts.getOrDefault(key, 0) > 0`

- [ ] **4.5** In `WebsocketChannelInterceptor`, on SUBSCRIBE to `/topic/games/{gameId}/{playerEmail}`:
  - You already parse gameId and playerEmail here (line 73+)
  - Call `registry.registerSession(sessionId, gameId, playerEmail)`
  - The sessionId is available from `accessor.getSessionId()`

- [ ] **4.6** Handle the UNSUBSCRIBE and DISCONNECT commands in the interceptor (or via EventListener — see Slice 5):
  - Call `registry.unregisterSession(sessionId)`

### Verification

- Player connects to a game → `isPlayerConnected` returns true
- Player opens second tab → count = 2, still connected
- Close one tab → count = 1, still connected
- Close second tab → count = 0, disconnected
- Player reconnects → count = 1, connected again

---

## Slice 5 — Backend: Disconnect Timer & Auto-Forfeit

**Goal:** If a player is fully disconnected for 2 minutes, auto-forfeit.

**Depends on:** Slice 4

### Tasks

- [ ] **5.1** Create `WebSocketEventListener` class (`@Component`) or add `@EventListener` methods to an existing class:
  - Inject `WebSocketSessionRegistry`, `GameSessionService`

- [ ] **5.2** Listen for `SessionDisconnectEvent`:
  - Get sessionId from the event
  - Call `registry.unregisterSession(sessionId)` — returns new count
  - If count == 0: the player is fully disconnected
    - Look up (gameId, playerEmail) from the session
    - Call `gameSessionService.scheduleDisconnectForfeit(gameId, playerEmail)`

- [ ] **5.3** Handle reconnection — on SUBSCRIBE (already handled in Slice 4.5):
  - After `registerSession`, if the previous count was 0 (player was disconnected):
    - Call `gameSessionService.cancelDisconnectForfeit(gameId, playerEmail)`

- [ ] **5.4** Add `Map<String, ScheduledFuture<?>> disconnectTimers` to `GameSessionService`:
  - Key: `"gameId:playerEmail"`

- [ ] **5.5** Create `scheduleDisconnectForfeit(UUID gameId, String playerEmail)`:
  - Schedule a 120s (2 minute) task that:
    1. Acquires game lock
    2. Verifies game is still in progress (not GAME_OVER)
    3. Calls `surrender(gameId, playerEmail)` — reuses existing surrender logic
    4. Broadcasts state
  - Also: broadcast a **presence message** to the opponent (see Slice 6)

- [ ] **5.6** Create `cancelDisconnectForfeit(UUID gameId, String playerEmail)`:
  - Cancel the scheduled future
  - Remove from map
  - Broadcast a presence message (player reconnected) — see Slice 6

- [ ] **5.7** Cancel disconnect timers when the game ends for any other reason:
  - In `surrender()`: cancel both players' disconnect timers
  - When game reaches GAME_OVER phase: cancel both

- [ ] **5.8** Edge case — what if the disconnected player's turn timer fires WHILE they're disconnected?
  - The turn timer auto-passes for them
  - The disconnect timer keeps running independently (they're still disconnected)
  - Both timers are independent — this is correct behavior. The disconnected player's turns get auto-passed, and if they don't reconnect in 2 minutes total, they forfeit.

### Verification

- Player A disconnects (close browser) → 2 minutes pass → game ends, Player B wins by surrender
- Player A disconnects → reconnects after 30s → game continues normally, no forfeit
- Player A disconnects → turn timer fires (auto-pass) → Player A still disconnected → disconnect timer still ticking
- Player A has two tabs, closes one → no forfeit timer (count > 0)
- Player A has two tabs, closes both → forfeit timer starts
- Game ends normally while a player is disconnected → disconnect timer cancelled (no double game-over)

---

## Slice 6 — Frontend: Opponent Disconnected Banner

**Goal:** Notify the remaining player that their opponent disconnected and show reconnection countdown.

**Depends on:** Slice 5

### Tasks

- [ ] **6.1** Define a presence message interface in `frontend/src/types/game.ts`:
  ```typescript
  interface PresenceMessage {
    playerEmail: string
    connected: boolean
    forfeitDeadlineUtc: number | null  // epoch millis, null if reconnected
  }
  ```

- [ ] **6.2** Backend: create a simple DTO for the presence message and send it via `SimpMessagingTemplate` to `/topic/games/{gameId}/presence`:
  - Send when a player disconnects (connected=false, forfeitDeadlineUtc set)
  - Send when a player reconnects (connected=true, forfeitDeadlineUtc=null)

- [ ] **6.3** In `useWebSocket.ts`, add a third STOMP subscription to `/topic/games/{gameId}/presence`:
  - On message: update a new piece of state (either in gameStore or local state in the hook)

- [ ] **6.4** Add presence state to `gameStore.ts` (or a new small store):
  ```typescript
  opponentConnected: boolean  // default true
  forfeitDeadlineUtc: number | null
  ```

- [ ] **6.5** Create `DisconnectedBanner` component at `frontend/src/components/board/overlays/DisconnectedBanner.tsx`:
  - Reads `opponentConnected` and `forfeitDeadlineUtc` from store
  - Only visible when `opponentConnected === false`
  - Shows: "Opponent disconnected. Auto-forfeit in X:XX"
  - Countdown uses same pattern as TurnCountdown (compute remaining from deadline)
  - Positioned at top of the board, doesn't block gameplay
  - Disappears when opponent reconnects

- [ ] **6.6** Style the banner:
  - Use `--red` background with some transparency
  - `--text-primary` text color
  - `--font-ui` (Barlow) for readability
  - Fixed position at top, z-index above board but below overlays

- [ ] **6.7** Mount `DisconnectedBanner` in `Game.tsx`

- [ ] **6.8** Reset presence state on game unmount (cleanup in useWebSocket or gameStore.reset)

### Verification

- Opponent disconnects → banner appears with countdown from 2:00
- Opponent reconnects → banner disappears immediately
- Countdown reaches 0:00 → game ends (handled by backend forfeit, GameOverOverlay appears)
- Navigate away from game → presence state cleaned up

---

## Slice 7 — Frontend: Game Over for Disconnect

**Goal:** Show appropriate message when game ends because opponent was disconnected.

**Depends on:** Slice 5

### Tasks

- [ ] **7.1** The backend currently calls `surrender()` for disconnect forfeits, which sets `endReason = SURRENDER`. To distinguish on the frontend, you have two options:
  - **Option A (simple):** Check if the `DisconnectedBanner` was visible when game ended — if opponent was disconnected and game ended by surrender, show disconnect message
  - **Option B (cleaner):** Add a `disconnectForfeit: boolean` field to GameStateDto from the backend, set it to true only when the surrender was triggered by a disconnect timer
  - Pick whichever feels right to you

- [ ] **7.2** Update `GameOverOverlay.tsx` to handle disconnect case:
  - If disconnect forfeit and you won: "Opponent disconnected — victory awarded"
  - If disconnect forfeit and you lost: "You were disconnected — game forfeited"
  - Use muted/neutral styling instead of the celebratory gold or harsh defeat red

- [ ] **7.3** If you went with Option A in 7.1, make sure the `opponentConnected` state persists long enough for `GameOverOverlay` to read it (don't reset it on GAME_OVER phase change)

### Verification

- Opponent disconnects → 2 min pass → GameOverOverlay shows "Opponent disconnected" message
- You disconnect → 2 min pass → on any reconnection attempt, game is over with "You were disconnected" message
- Normal surrender → GameOverOverlay shows regular victory/defeat (no disconnect wording)
- Normal game end → GameOverOverlay shows regular victory/defeat

---

## Development Order

```
Track A (Turn Timer)              Track B (Disconnect Handling)
========================          ============================
Slice 1 - Turn Timer Infra       Slice 4 - Session Tracking
         |                                 |
Slice 2 - Deadline in DTO        Slice 5 - Disconnect & Forfeit
         |                                 |
Slice 3 - Countdown UI           Slice 6 - Disconnected Banner
                                           |
                                  Slice 7 - Game Over Disconnect
```

Tracks A and B are independent. You can build them in parallel or sequentially — your choice.

Within each track, go top to bottom.
