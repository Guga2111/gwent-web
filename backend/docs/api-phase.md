
# gwent-api — Architecture, Patterns & Roadmap

## Context

`gwent-api` is the Spring Boot 3.4 transport layer that sits between clients and `gwent-engine`.
Its only job for game sessions is to translate HTTP/WebSocket signals into engine commands and
broadcast the resulting state back. For non-game features (decks, collections, quests, chests)
it is a standard Spring Boot application with REST + PostgreSQL.

`gwent-engine` is a dependency of `gwent-api` — not the other way around. The engine has zero
knowledge of Spring, HTTP, or WebSocket.

---

## Architecture decision: Modular Layered (Package by Feature)

### Why not Hexagonal

Hexagonal (Ports & Adapters) is a natural fit for the game session boundary, but:
- High learning curve for all other features (collections, decks, quests, chests)
- Those features are standard CRUD — hexagonal adds ceremony with zero benefit
- Premature for a project at this stage

### Why not classic Layered (package by layer)

`controllers/`, `services/`, `repositories/` at the root grows into a navigation nightmare as
features are added. A change to "deck" touches files spread across three top-level packages.

### Decision: Package by Feature

Each feature is a self-contained package. A developer opens `deck/` and finds everything related
to decks without touching other packages.

```
gwent-api/src/main/java/com/gwent/api/
  game/
    GameController.java          ← WebSocket handler
    GameSessionService.java      ← orchestrates engine calls, manages sessions
    GameSessionRepository.java   ← persists session snapshots
    dto/
      GameStateDto.java
      CommandRequestDto.java
      ErrorDto.java
  deck/
    DeckController.java
    DeckService.java
    DeckRepository.java
    dto/
      DeckDto.java
      DeckCardDto.java
  collection/
    CollectionController.java
    CollectionService.java
    CollectionRepository.java
    dto/
      CollectionDto.java
  auth/
    AuthController.java
    AuthService.java
    JwtService.java
    dto/
      LoginRequestDto.java
      TokenResponseDto.java
  quest/         ← post-MVP
  chest/         ← post-MVP
  shared/
    exception/
      GlobalExceptionHandler.java
      GwentApiException.java
    config/
      WebSocketConfig.java
      SecurityConfig.java
      CorsConfig.java
```

### The key boundary rule

> `game/` is the **only** package allowed to import `gwent-engine` classes.
> All other features are standard Spring Boot — they never touch the engine.

---

## Tech stack

| Concern              | Choice                                      |
|----------------------|---------------------------------------------|
| Framework            | Spring Boot 3.4                             |
| Language             | Java 25                                     |
| Real-time transport  | WebSocket (Spring WebSocket + STOMP)        |
| REST                 | Spring MVC                                  |
| Database (prod)      | PostgreSQL via Supabase                     |
| Database (dev)       | PostgreSQL via Docker (`docker-compose-dev.yml`) |
| Database (CI/tests)  | H2 in-memory                               |
| Auth                 | JWT (hand-rolled, no OAuth for now)         |
| ORM                  | Spring Data JPA + Hibernate                 |
| Build                | Maven multi-module                          |
| Containerization     | Docker + Docker Compose                     |

---

## Game session design

### Transport split

| Action                        | Transport  |
|-------------------------------|------------|
| Create game session           | REST POST  |
| Join game session             | REST POST  |
| Send command (play, pass...)  | WebSocket  |
| Receive state updates         | WebSocket  |
| Get current state (reconnect) | REST GET   |

### Command flow

```
Client → WebSocket message (JSON CommandRequestDto)
  → GameController deserializes to GameCommand
  → GameSessionService.execute(gameId, playerId, command)
    → validates it is the right player
    → engine.execute(state, command)
    → persists updated state snapshot
    → broadcasts GameStateDto to both players
```

### Mulligan timeout

When a game session enters `REDRAW` phase, `GameSessionService` schedules a 30-second task:

```java
scheduler.schedule(() -> {
    if (state.getPhase() == GamePhase.REDRAW) {
        engine.startPlay(state);
        broadcastState(gameId);
    }
}, 30, TimeUnit.SECONDS);
```

If both players confirm before the timer fires, the phase is already `PLAY` and the task is a no-op.
This logic lives entirely in `GameSessionService` — the engine has no concept of time.

### Session persistence

Game sessions are stored in the database so:
- A server restart does not kill active games
- Match history is available after game ends
- Future features (stats, replays) have the data

State is serialized as JSON (Jackson) into a `TEXT` column. Simple and sufficient for this scale.

---

## Profiles

| Profile | Database              | Engine logs | Notes                      |
|---------|-----------------------|-------------|----------------------------|
| `dev`   | Postgres (Docker)     | verbose     | local development          |
| `test`  | H2 in-memory          | minimal     | CI and unit/integration    |
| `prod`  | Postgres (Supabase)   | errors only | deployed on VPS            |

`application.yml` + `application-{profile}.yml` per profile. Secrets injected via environment
variables — never hardcoded, never committed.

---

## Auth (MVP)

- Register / Login returns a JWT (hand-rolled with `jjwt`)
- JWT validated on every WebSocket connection handshake and REST request via a filter
- Player identity extracted from JWT in `GameSessionService` to validate whose turn it is
- No refresh tokens, no OAuth, no roles for MVP — keep it simple

---

## MVP scope

The following must work end-to-end before anything else is built:

1. **Auth** — register, login, JWT issued
2. **Game session** — create game, join game, play a full match via WebSocket using a preset deck
3. **Preset deck** — one hardcoded deck per faction seeded into the database at startup (no deck builder for MVP)

Out of scope for MVP (implement after):
- Deck builder (CRUD for custom decks)
- Collection system
- Quests
- Chests
- Match history UI
- Player profiles / stats

---

## Patterns

### DTOs at the boundary

Domain objects (`Card`, `GameState`, `PlayerState`) are **never** serialized directly to the
client. Always map to a DTO before sending. The client contract is decoupled from internal
representation.

### Service owns session logic

`GameSessionService` is the only place that:
- Calls `engine.execute()`
- Calls `engine.startPlay()` (timeout)
- Persists state
- Broadcasts to WebSocket subscribers

Controllers are thin — they deserialize the request, call the service, and return.

### Map GwentException to client errors

`GlobalExceptionHandler` catches all `GwentException` subtypes and maps them to typed
`ErrorDto` responses. The client always receives a structured error, never a raw stack trace.

### Environment variables for all secrets

DB URL, JWT secret, allowed origins — all via env vars. Docker run receives them at startup.
Documented in `.env.example`. `.env` is gitignored.

---

## Anti-patterns

### Do not call the engine from outside `game/`

No other package imports `gwent-engine`. Collections, decks, quests have no business talking
to the engine.

### Do not expose domain objects over the wire

Never return `Card`, `GameState`, or any `gwent-engine` class from a controller or WebSocket
handler. Always use a DTO.

### Do not put game logic in `GameController`

The controller deserializes and delegates. All decisions (whose turn, is move valid, phase
transition) happen inside the engine or `GameSessionService`.

### Do not hardcode secrets

No passwords, JWT secrets, or DB URLs in source code or committed files.

### Do not use a single `application.yml` for everything

Profile separation (`dev`, `test`, `prod`) must be respected. H2 must never be used in prod.
Supabase credentials must never be used in tests.

### Do not skip the DTO layer to save time

Exposing JPA entities directly leads to circular serialization, leaked internals, and breaking
API changes. DTOs are not optional.

---

## DevOps & CI/CD

### Branch strategy

| Branch | Purpose                        |
|--------|--------------------------------|
| `dev`  | integration — all PRs land here first |
| `main` | production — only from `dev` via PR  |

### GitHub Actions workflows

#### `.github/workflows/ci.yml` — runs on every PR to `dev` and `main`

```
Trigger: pull_request targeting dev or main
Steps:
  1. Checkout
  2. Set up Java 25
  3. Run mvn test (engine + api) with profile=test (H2)
  4. Fail PR if any test fails
```

#### `.github/workflows/deploy.yml` — runs on every PR merge to `main`

```
Trigger: push to main (after PR merge)
Steps:
  1. Checkout
  2. Set up Java 25
  3. Run mvn test — gate before deploy
  4. Build Docker image
  5. Push image to GitHub Container Registry (ghcr.io)
  6. SSH into VPS
  7. docker pull ghcr.io/[user]/gwent-api:latest
  8. docker stop gwent-api && docker rm gwent-api
  9. docker run with env vars from GitHub Secrets
  10. Health check — curl the /actuator/health endpoint
```

### Why GitHub Container Registry (ghcr.io)

The FutspringV2 pattern builds and SCP's a tar.gz to the VPS. For CI/CD this does not work
because GitHub Actions runners cannot SCP large files efficiently. Instead:
- GitHub Actions builds and pushes the image to ghcr.io (free for public repos)
- The VPS SSH step pulls the image directly from ghcr.io
- No large file transfer between runner and VPS

### Secrets required in GitHub

| Secret                | Description                        |
|-----------------------|------------------------------------|
| `VPS_HOST`            | VPS IP address                     |
| `VPS_USER`            | SSH user (root)                    |
| `VPS_SSH_KEY`         | Private SSH key for VPS access     |
| `DB_URL`              | Supabase JDBC URL                  |
| `DB_USERNAME`         | Supabase DB username               |
| `DB_PASSWORD`         | Supabase DB password               |
| `JWT_SECRET`          | JWT signing secret                 |
| `ALLOWED_ORIGINS`     | CORS allowed origins               |
| `GHCR_TOKEN`          | GitHub token with packages:write   |

### VPS setup (one-time, manual)

The VPS already has Docker and Nginx. For this project:
1. Create `/root/projects/gwent/` directory
2. Add Nginx server block for `gwent.luisgosampaio.com` proxying to port `8082`
3. Run `certbot --nginx -d gwent.luisgosampaio.com`
4. Add the VPS SSH key to GitHub Secrets

Nginx block pattern (same as other projects):
```nginx
server {
    server_name gwent.luisgosampaio.com;
    location / {
        proxy_pass http://localhost:8082;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

The `Upgrade` and `Connection` headers are required for WebSocket proxying through Nginx.

### Docker setup

`Dockerfile` (multi-stage, builds the fat JAR):
```
Stage 1: maven:3.9-eclipse-temurin-25 → mvn package -DskipTests
Stage 2: eclipse-temurin:25-jre → copy JAR, EXPOSE 8080, ENTRYPOINT
```

`docker-compose-dev.yml` (local development only):
```
services:
  db:   postgres:16, port 5432, volume for data
  api:  builds from Dockerfile, depends_on db, profile=dev
```

No `docker-compose.yml` for production — the VPS runs the container directly via `docker run`
with env vars injected by the GitHub Actions deploy step.

---

## Architecture — future improvements (not MVP)

### WebSocket/STOMP authentication

Currently the `/ws/**` endpoint is `permitAll()` in `SecurityConfig` and there is no
`ChannelInterceptor` in `WebSocketConfig`. This means any client can connect via WebSocket
and send/receive STOMP messages without a JWT.

The HTTP `JWTAuthorizationFilter` only runs on the initial HTTP handshake, and since `/ws/**`
is permitted, no authentication happens at all — neither on the handshake nor on subsequent
STOMP frames.

**How to fix:**
1. Add a `ChannelInterceptor` to `WebSocketConfig` that intercepts the STOMP `CONNECT` frame
2. Extract the JWT from the STOMP `Authorization` header (or a custom `token` header)
3. Validate the JWT using the same logic as `JWTAuthorizationFilter`
4. Set the `Authentication` in the STOMP session's `SecurityContext`
5. Reject the connection if the token is missing or invalid

```java
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = accessor.getFirstNativeHeader("Authorization");
                // validate JWT, set Authentication principal
            }
            return message;
        }
    });
}
```

**When to implement:** before exposing the app to real users. For local development with
trusted clients this is acceptable, but any public deploy must authenticate WebSocket connections.

---

### Player faction in PlayerState

Currently `Faction` exists as a domain enum and each `Card` carries its faction, but
`PlayerState` has no `faction` field. The player's faction is implicit — inferred from
the leader card or deck composition — but never stored explicitly.

This matters because faction passive abilities are per-player, not per-card:
- **Monsters:** keep one random card on the board between rounds
- **Northern Realms:** draw an extra card when winning a round
- **Nilfgaard:** win on a tie
- **Scoia'tael:** choose who goes first each round
- **Skellige:** (TBD)

**Where it belongs:** `PlayerState.faction` in `gwent-engine`. The engine needs the faction
to resolve round transitions and passive abilities — having it live in the API would force
the engine to receive it as an external parameter on every operation.

**How to implement:**
1. Add `Faction faction` field to `PlayerState`
2. Set it at game creation time when the API builds the `PlayerState` from the player's deck
3. Use it in the engine during round-end logic to apply faction passives
4. Expose it in `PlayerStateDto` so the frontend can display faction crests/colors

**Faction passive abilities to implement** (resolved in `startNewRound` / `resolveRound`):
- **Monsters:** keep one random unit card on the board between rounds (skip `clearRows` for that card)
- **Northern Realms:** draw 1 extra card from deck when winning the round
- **Nilfgaard:** win the round on a tie (instead of both players losing a life)
- **Scoia'tael:** choose who goes first each round (prompt player instead of auto-assigning loser)
- **Skellige:** (TBD — resurrect random cards from graveyard in Witcher 3 variant)

**When to implement:** when faction passive abilities are added to the engine. For MVP with
no faction passives, the field is not strictly needed.

---

### Redis as session store

`GameSessionService` currently uses a `ConcurrentHashMap<UUID, GameState>` as the runtime
session store. This works for a single server instance but breaks under horizontal scaling —
each instance has its own map and a request routed to a different instance won't find the session.

**When to migrate:** when a second server instance is needed or when server restarts losing
active sessions becomes a real problem for users.

**How to migrate:**
1. Add `spring-boot-starter-data-redis` to `gwent-api/pom.xml`
2. Write a `GameStateSerializer` that converts `GameState` ↔ JSON (or use Java serialization)
3. Replace `ConcurrentHashMap` in `GameSessionService` with a `RedisTemplate<UUID, GameState>`
4. Add Redis to `docker-compose-dev.yml` and to the VPS deploy

The change is entirely contained in `GameSessionService` — engine, controllers, and DTOs are
untouched.

---

## DevOps — future improvements (not MVP)

These were consciously deferred. Implement when the project matures.

### Health check in deploy workflow

Currently `deploy.yml` only checks `docker ps` after starting the container. A proper health
check would hit the `/actuator/health` endpoint and fail the deploy if the app did not come up:

```yaml
- name: Health check
  run: |
    sleep 15
    curl --fail https://gwent.luisgosampaio.com/actuator/health || exit 1
```

Requires adding `spring-boot-starter-actuator` to `gwent-api` and exposing the health endpoint.
The deploy workflow step that currently does `docker logs --tail 30` should be replaced with this.

### Rollback strategy

The deploy has no automatic rollback if the container crashes after starting. Every image is
already tagged with `${{ github.sha }}` in addition to `latest`, so the previous image is
always available on ghcr.io. A rollback step would:

1. Record the previous image SHA before deploying (e.g. `docker inspect gwent-api`)
2. On health check failure, `docker stop gwent-api && docker rm gwent-api`
3. `docker run` with the previous SHA tag instead of `latest`

Not needed until there are real users who would be affected by a failed deploy.

### Docker layer caching in CI

Every run of `deploy.yml` re-downloads all Maven dependencies from scratch because GitHub
Actions runners are ephemeral. Speed it up with:

```yaml
- name: Cache Maven dependencies
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-maven-
```

Add this step before the `mvn` calls in both `ci.yml` and `deploy.yml`. Saves ~30-60s per run.

### Separate staging environment

A `staging` branch that auto-deploys to `gwent-staging.luisgosampaio.com` before anything
reaches `main`. Useful when there are real users on prod and regressions would be disruptive.
Requires a second VPS subdomain, Nginx block, and a third GitHub Actions workflow.
Total overkill until the project has active users.

---

## Implementation order

1. **DevOps** — GitHub Actions CI/CD, Dockerfile, Docker Compose ✅
2. **`gwent-api` module** — Maven module + Spring Boot skeleton, profiles, DB config
3. **`game/`** — WebSocket, session creation, engine command dispatch, preset deck seed
       (players identified by hardcoded IDs at this stage — no auth yet)
4. **End-to-end test** — two players complete a full match through the WebSocket API
5. **`auth/`** — register, login, JWT issued; replace hardcoded player identity with JWT claim
6. **`deck/`** — deck builder CRUD (post-MVP)
7. **`collection/`** — post-MVP
8. **`quest/` + `chest/`** — post-MVP

> `auth/` intentionally comes after `game/` is working end-to-end. The game session does not
> need real authentication to prove the engine integration works. Auth slots in cleanly at
> step 5 — `GameSessionService` already expects a player identifier, swapping a hardcoded
> string for a JWT-extracted claim is a minimal change.
