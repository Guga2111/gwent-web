# gwent-web

Loosely coupled Witcher 3 Gwent card game — engine, API, client, and other services.

## Project Structure

```
gwent-web/
└── backend/                    # Maven multi-module (Java 25)
    ├── pom.xml                 # Parent POM
    ├── gwent-engine/           # Pure Java game engine (no dependencies)
    │   └── src/main/java/com/gwent/engine/
    │       ├── domain/         # Immutable data definitions (enums, records)
    │       ├── state/          # Mutable game state (BoardRow, PlayerState, GameState)
    │       ├── command/        # Player actions (PlayCardCommand, etc.) [planned]
    │       ├── core/           # Game rules and orchestration [planned]
    │       └── exception/      # Game-specific exceptions [planned]
    └── gwent-api/              # Spring Boot REST + WebSocket API (depends on engine)
```

## Architecture

### gwent-engine

The engine is a standalone Java library with zero external dependencies. It models the Witcher 3 Gwent card game rules.

#### `domain` — What things are

| Class | Type | Description |
|-------|------|-------------|
| `Card` | record | Card definition with id, name, faction, type, ability, row, power. Compact constructor validates field combinations and normalizes the id. |
| `Faction` | enum | MONSTER, NILFGAARD, NORTHERN_REALMS, SCOIATAEL, SKELLIGE, NEUTRAL |
| `CardType` | enum | UNIT, HERO, SPECIAL, WEATHER, LEADER |
| `RowType` | enum | MELEE, RANGED, SIEGE |
| `Ability` | enum | SPY, MEDIC, TIGHT_BOND, MORALE_BOOST, MUSTER, BERSERKER, SCORCH, AGILE, DUMMY, COMMANDERS_HORN |
| `LeaderAbility` | enum | Unique abilities for each leader card (18 total across all factions) |
| `GamePhase` | enum | COIN_FLIP → REDRAW → PLAY → ROUND_END → REDRAW/GAME_OVER |
| `Turn` | enum | PLAYER_1, PLAYER_2 |

#### `state` — How things look right now

| Class | Description |
|-------|-------------|
| `BoardRow` | Holds a list of cards for one row (melee/ranged/siege), tracks horn and weather effects. Validates card row matches board row. |
| `PlayerState` | Player's hand, deck (Deque), graveyard, 3 board rows, leader, lives (starts at 2), passed status. Clearing rows moves cards to graveyard. |
| `Board` | Holds two PlayerStates and shared active weather cards. |
| `GameState` | Top-level game state: board, both players, current turn, round (1-3), and phase. Enforces phase transition order and round limits. Turn is null until coin flip. |

#### `command` — What players can do [planned]

Player actions like playing a card, passing, using leader ability.

#### `core` — How the game works [planned]

GameEngine and RuleProcessor — validates moves, applies abilities, calculates strength, determines round/game winners.

#### `exception` — What can go wrong [planned]

Game-specific exceptions for rule violations.

### gwent-api

Spring Boot application with REST and WebSocket support. Depends on gwent-engine.

## Build & Test

```bash
# Build everything
mvn clean compile

# Run tests
mvn test

# Run only engine tests
mvn test -pl gwent-engine
```

## Key Design Decisions

- **Engine has zero dependencies** — can be tested and used independently from Spring
- **State classes are dumb containers** — game logic will live in `core`, not in state objects
- **Card is a record** — immutable, with compact constructor validation
- **Nullable fields over sentinel values** — weather/special cards have `null` for rowType/basePower instead of `NONE`/`0`
- **Phase transitions are enforced** — GameState validates the order of game phases
- **Deck uses Deque** — natural stack semantics for drawing cards
- **Leader abilities are separate from unit abilities** — different enum, different behavior
