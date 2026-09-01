# Frontend Refactoring Plan — SRP & Hook Extraction

> **Goal:** enforce Single Responsibility Principle by extracting inline logic from pages/components into dedicated custom hooks, aligning with the patterns defined in `patterns.md`.

---

## Table of Contents

1. [Diagnosis Summary](#1-diagnosis-summary)
2. [Refactoring: Game.tsx](#2-refactoring-gametsx)
3. [Refactoring: Taverna.tsx](#3-refactoring-tavernatsx)
4. [Refactoring: DeckForge.tsx](#4-refactoring-deckforgetsx)
5. [Refactoring: MesaPrivadaModal.tsx](#5-refactoring-mesaprivadamodaltsx)
6. [Components Already Correct](#6-components-already-correct)
7. [Minor Cleanup](#7-minor-cleanup)
8. [New Hooks Summary](#8-new-hooks-summary)
9. [Implementation Order](#9-implementation-order)
10. [Testing Checklist](#10-testing-checklist)

---

## 1. Diagnosis Summary

### What `patterns.md` says

- Pages and hooks access stores; components receive data via props.
- Stores hold state only — no network calls, no side effects.
- Commands go via WebSocket, session operations via HTTP.
- Clear separation: `api/ → hooks/ → stores/ → pages/ → components/`.

### What the codebase currently does

| File | Lines | Problem |
|---|---|---|
| `pages/Game.tsx` | ~575 | God component: card selection, play logic, flying card animation, board inspection, weather targeting, error auto-dismiss — all inline |
| `pages/hub/Taverna.tsx` | ~320 | API calls (deck fetch, catalog fetch, matchmaking), state management, and UI rendering all mixed together |
| `pages/hub/DeckForge.tsx` | ~183 | Full CRUD logic (create/update/delete deck, add/remove card), validation, catalog fetching — all inline |
| `components/hub/MesaPrivadaModal.tsx` | ~290 | A component that fetches its own data (`getUserDecks`), manages loading/error states, and handles create/join game flows |

### Core principle being violated

**Single Responsibility Principle:** each module should have one reason to change. Currently, pages change when UI layout changes *and* when game logic changes *and* when animation logic changes. Extracting hooks decouples these concerns.

---

## 2. Refactoring: Game.tsx

`Game.tsx` is the biggest offender at ~575 lines with 5+ distinct responsibilities mixed together. It should be split into the following hooks:

### 2.1 — Extract `useCardSelection`

**What it owns:** which card is selected in hand, which board card is being inspected, which leader is being inspected, and the mutual exclusion logic between them.

**File:** `hooks/useCardSelection.ts`

**State to move:**
- `selectedCardId` (line 47)
- `inspectedBoardCard` (line 48)
- `inspectedLeader` (line 49)

**Logic to move:**
- `handleBoardCardClick` (lines 106-110)
- `handleLeaderClick` (lines 112-118)
- The `useEffect` that clears other inspections when a hand card is selected (lines 99-104)
- Hand card click toggle logic (currently inline in JSX, lines 383-394)

**Hook signature:**
```ts
function useCardSelection() {
  // ...
  return {
    selectedCardId,
    selectedCard,        // derived: me?.hand.find(c => c.id === selectedCardId)
    inspectedBoardCard,
    inspectedLeader,
    selectHandCard,      // (cardId: string) => void — toggles, clears others
    inspectHandCard,     // (card: CardDto) => void — for when it's not your turn
    inspectBoardCard,    // (card: CardDto) => void — toggles
    inspectLeader,       // (card: CardDto, side: 'player'|'opponent') => void
    clearSelection,      // () => void
  }
}
```

### 2.2 — Extract `useGameActions`

**What it owns:** sending game commands (play card, pass, use leader, confirm play) and row targeting validation.

**File:** `hooks/useGameActions.ts`

**Logic to move:**
- `handlePlayWeatherCard` (lines 120-129)
- `handleConfirmPlay` (lines 131-140)
- `canPlayOnRow` (lines 142-148)
- `handlePlayCard` (lines 150-175) — the command-sending part only; flying card launch moves to `useFlyingCard`

**Dependencies:** receives `sendCommand` from `useWebSocket`, reads `playerId` from auth store, reads `isMyTurn` and `selectedCard` from game state / card selection.

**Hook signature:**
```ts
function useGameActions(
  sendCommand: (cmd: CommandRequest) => void,
  gameState: GameStateDto | null,
  selectedCard: CardDto | null,
  clearSelection: () => void,
  launchFlyingCard?: (card: CardDto, targetRow: RowType) => void,
) {
  // ...
  return {
    isMyTurn,
    canInteract,      // isMyTurn && phase === 'PLAY' && !pendingAbility
    playCard,          // (targetRow: RowType) => void
    playWeatherCard,   // () => void
    confirmPlay,       // () => void
    pass,              // () => void
    useLeader,         // () => void
    canPlayOnRow,      // (row: RowType) => boolean
  }
}
```

### 2.3 — Extract `useFlyingCard`

**What it owns:** the flying card animation lifecycle — launch, track when the card appears in the row data (WS update), track when the CSS animation ends, coordinate both to clear the overlay.

**File:** `hooks/useFlyingCard.ts`

**State to move:**
- `flyingCard` (lines 53-58)
- `landedCardId` (line 59)
- `flyingAnimDone` ref (line 60)
- `flyingCardInRow` ref (line 61)

**Logic to move:**
- The DOM rect calculation and launch (lines 153-166 from `handlePlayCard`)
- The `useEffect` that checks if the card has arrived in row data (lines 178-196)
- The `useEffect` that clears on error (lines 199-204)
- `handleFlightComplete` (lines 206-211)
- `clearFlyingCard` (lines 213-220)

**Hook signature:**
```ts
function useFlyingCard(
  me: PlayerStateDto | undefined,
  opponent: OpponentStateDto | undefined,
  error: string | null,
) {
  // ...
  return {
    flyingCard,           // animation state (card, fromRect, toRect, targetRow) or null
    landedCardId,         // briefly set after landing for suppress-enter animation
    launchCard,           // (card: CardDto, targetRow: RowType) => void
    handleFlightComplete, // () => void — called by FlyingCard onComplete
  }
}
```

### 2.4 — Extract `useErrorAutoDismiss`

**What it owns:** auto-clearing the game error after a timeout.

**File:** `hooks/useErrorAutoDismiss.ts`

**Logic to move:**
- The `useEffect` at lines 79-83

**Hook signature:**
```ts
function useErrorAutoDismiss(durationMs?: number): void
// Reads error/setError from gameStore internally
```

**Note:** this is a small hook. It could also live as a utility inside `useGameActions` or remain inline if preferred. Extract only if the pattern repeats elsewhere.

### 2.5 — Extract `useRevealedCards`

**What it owns:** controlling visibility of the EMPEROR_OF_NILFGAARD revealed cards overlay.

**File:** `hooks/useRevealedCards.ts`

**Logic to move:**
- `showRevealedCards` state (line 50)
- The `useEffect` that shows the overlay when `revealedCards` arrives (lines 64-68)

**Hook signature:**
```ts
function useRevealedCards(revealedCards: CardDto[] | undefined) {
  return { showRevealedCards, dismissRevealedCards }
}
```

### 2.6 — Resulting Game.tsx

After all extractions, `Game.tsx` should be ~200-250 lines of pure composition:

```tsx
export default function Game() {
  const { gameId } = useParams()
  const navigate = useNavigate()
  const gameState = useGameStore((s) => s.gameState)
  const connected = useGameStore((s) => s.connected)
  const error = useGameStore((s) => s.error)
  const setGameId = useGameStore((s) => s.setGameId)
  const reset = useGameStore((s) => s.reset)

  const { sendCommand } = useWebSocket(gameId ?? null)
  const { remainingPct, remainingSeconds, isUrgent } = useTurnCountdown(...)

  const {
    selectedCardId, selectedCard, inspectedBoardCard, inspectedLeader,
    selectHandCard, inspectHandCard, inspectBoardCard, inspectLeader, clearSelection,
  } = useCardSelection()

  const { flyingCard, landedCardId, launchCard, handleFlightComplete } = useFlyingCard(...)
  const { showRevealedCards, dismissRevealedCards } = useRevealedCards(...)

  const {
    isMyTurn, canInteract, playCard, playWeatherCard, confirmPlay,
    pass, useLeader, canPlayOnRow,
  } = useGameActions(sendCommand, gameState, selectedCard, clearSelection, launchCard)

  useErrorAutoDismiss()

  useEffect(() => { ... }, [gameId])  // setGameId + cleanup

  // Only JSX below — no handler definitions
  return (...)
}
```

---

## 3. Refactoring: Taverna.tsx

### 3.1 — Extract `useTavernaData`

**What it owns:** fetching user decks, selecting the active deck, fetching the leader card art for the preview.

**File:** `hooks/useTavernaData.ts`

**State to move:**
- `decks` (line 30)
- `activeDeck` (line 31)
- `leaderCard` (line 32)

**Logic to move:**
- `selectDeck` function (lines 36-43)
- The `useEffect` that fetches decks on mount (lines 45-49)

**Hook signature:**
```ts
function useTavernaData() {
  return {
    decks,
    activeDeck,
    leaderCard,
    selectDeck,  // (deck: DeckDto) => void
  }
}
```

### 3.2 — Extract `useMatchmaking`

**What it owns:** the matchmaking flow — searching, cancelling, error state, modal open/close.

**File:** `hooks/useMatchmaking.ts`

**State to move:**
- `matchmakingOpen` (line 28)

**Logic to move:**
- `handleSearchOpponent` (lines 65-81)
- `handleCancelMatchmaking` (lines 83-87)
- The `useEffect` that resets matchmaking store on unmount (lines 52-54)

**Dependencies:** reads `activeDeck` from the taverna data hook or hubStore, reads/writes to `matchmakingStore`.

**Hook signature:**
```ts
function useMatchmaking(activeDeckId: string | null) {
  return {
    matchmakingOpen,
    matchmakingError,
    searchOpponent,    // () => Promise<void>
    cancelMatchmaking, // () => Promise<void>
  }
}
```

### 3.3 — Resulting Taverna.tsx

After extraction, `Taverna.tsx` becomes purely presentational + composition:

```tsx
export default function Taverna() {
  const [modalOpen, setModalOpen] = useState(false)
  const [pickerOpen, setPickerOpen] = useState(false)
  const { decks, activeDeck, leaderCard, selectDeck } = useTavernaData()
  const { matchmakingOpen, matchmakingError, searchOpponent, cancelMatchmaking } = useMatchmaking(activeDeck?.id ?? null)
  const setTab = useHubStore((s) => s.setTab)

  // Only JSX below
  return (...)
}
```

---

## 4. Refactoring: DeckForge.tsx

### 4.1 — Extract `useDeckEditor`

**What it owns:** the full deck editor lifecycle — CRUD operations, adding/removing cards, validation, catalog fetching.

**File:** `hooks/useDeckEditor.ts`

**State to move:**
- `decks` (line 31)
- `editor` (line 32)
- `catalog` (line 33)
- `saving` (line 34)
- `error` (line 35)

**Logic to move:**
- `openNew`, `openEdit`, `closeEditor` (lines 46-59)
- `handleDelete` (lines 61-64)
- `handleSave` (lines 66-97) — includes validation
- `addCard`, `removeCard` (lines 99-120)
- The `useEffect` that fetches decks on mount (lines 37-39)
- The `useEffect` that fetches catalog when faction changes (lines 41-44)
- Derived values: `leaders`, `nonLeaderCatalog`, `cardById` (lines 122-124)

**Hook signature:**
```ts
interface UseDeckEditorReturn {
  // List view
  decks: DeckDto[]
  // Editor state
  editor: EditorState | null
  catalog: CatalogCardDto[]
  leaders: CatalogCardDto[]
  nonLeaderCatalog: CatalogCardDto[]
  cardById: Record<string, CatalogCardDto>
  saving: boolean
  error: string
  // Actions
  openNew: () => void
  openEdit: (deck: DeckDto) => void
  closeEditor: () => void
  handleDelete: (id: string) => Promise<void>
  handleSave: () => Promise<void>
  addCard: (card: CatalogCardDto) => void
  removeCard: (cardId: string) => void
  setEditorField: (field: Partial<EditorState>) => void
}

function useDeckEditor(): UseDeckEditorReturn
```

### 4.2 — Resulting DeckForge.tsx

```tsx
export default function DeckForge() {
  const {
    decks, editor, catalog, leaders, nonLeaderCatalog, cardById,
    saving, error, openNew, openEdit, closeEditor,
    handleDelete, handleSave, addCard, removeCard, setEditorField,
  } = useDeckEditor()

  if (!editor) {
    return (/* list view JSX */)
  }

  return (/* editor view JSX */)
}
```

---

## 5. Refactoring: MesaPrivadaModal.tsx

### 5.1 — Extract `useMesaPrivada`

**What it owns:** fetching decks when modal opens, managing create/join flow states.

**File:** `hooks/useMesaPrivada.ts`

**Current violations:**
- The component calls `getUserDecks()` directly (line 29) — components should not fetch data.
- It manages its own `loading`, `error`, `decks`, `createdId`, `joinCode`, `showJoinInput` states.

**State to move:**
- `createdId`, `joinCode`, `showJoinInput`, `loading`, `error`, `decks`, `selectedDeckId`

**Logic to move:**
- `resetState` (lines 41-47)
- `handleCreate` (lines 54-69)
- `handleJoin` (lines 71-87)
- The `useEffect` that fetches decks on open (lines 27-33)
- The `useEffect` that syncs `defaultDeckId` (lines 35-37)

**Hook signature:**
```ts
function useMesaPrivada(
  open: boolean,
  defaultDeckId: string | null,
  onCreateGame: (deckId: string) => Promise<string>,
  onJoinGame: (code: string, deckId: string) => Promise<void>,
) {
  return {
    decks,
    selectedDeckId,
    setSelectedDeckId,
    createdId,
    joinCode,
    setJoinCode,
    showJoinInput,
    setShowJoinInput,
    loading,
    error,
    handleCreate,
    handleJoin,
    handleClose,  // resets state + calls parent onClose
  }
}
```

**Note:** This is a moderate-priority refactor. The modal is self-contained and not reused, so the coupling is limited. But it still violates the pattern of components not fetching data.

---

## 6. Components Already Correct

These files follow the patterns correctly and need **no changes**:

| File | Why it's fine |
|---|---|
| `components/board/overlays/MedicOverlay.tsx` | Pure presentational. Receives data via props, fires callback. |
| `components/board/overlays/LeaderOverlay.tsx` | Pure presentational. Config-driven, no data fetching. |
| `components/board/overlays/ScoiataelOverlay.tsx` | Pure presentational. Local `submitted` state is scoped UI guard. |
| `components/board/overlays/MulliganOverlay.tsx` | Local `selected` set is scoped UI state, not game state duplication. |
| `components/board/overlays/GameOverOverlay.tsx` | Pure presentational. All data via props. |
| `components/board/overlays/RoundEndOverlay.tsx` | Self-contained timer is purely visual (auto-dismiss). |
| `components/board/overlays/DisconnectedBanner.tsx` | Self-contained countdown is purely visual. |
| `components/board/controls/ControlBar.tsx` | Local `showDialog` is scoped UI state. |
| `components/board/controls/SurrenderDialog.tsx` | Pure presentational. |
| `components/hub/DeckPickerModal.tsx` | Pure presentational. All data via props. |
| `components/hub/MatchmakingModal.tsx` | Reads from stores (acceptable for modal at page-level), delegates to `useMatchmakingSocket` hook. |
| `pages/Login.tsx` | Minimal logic, appropriate for a simple form page. |
| `pages/Hub.tsx` | Thin composition shell, delegates to tab pages. |
| `hooks/useWebSocket.ts` | Clean hook: manages STOMP connection lifecycle. |
| `hooks/useTurnCountdown.ts` | Clean hook: single responsibility (countdown timer). |
| `hooks/useTutorial.ts` | Clean hook: single responsibility (tutorial state). |
| `hooks/useMatchmakingSocket.ts` | Clean hook: manages matchmaking WebSocket. |
| `stores/gameStore.ts` | Pure state, no side effects. |
| `stores/hubStore.ts` | Pure state, no side effects. |
| `stores/matchmakingStore.ts` | Pure state, no side effects. |

---

## 7. Minor Cleanup

These are small improvements to apply alongside or after the hook extractions.

### 7.1 — Remove `console.log` from `useTurnCountdown.ts`

Line 21: `console.log('[turn-timer] deadline:', turnDeadlineUtc)` — remove debug log.

### 7.2 — `hubStore.setTab` pattern

`hubStore` stores a callback function (`setTab`) registered by `Hub.tsx`. This is an unusual pattern — a store holding a function ref to imperatively call a React state setter. Consider replacing with storing `activeTab` directly in the store, so both `Hub.tsx` and `Taverna.tsx` read/write from the same source without the callback indirection.

**Current (indirect):**
```ts
// hubStore
setTab: null,
registerSetTab: (fn) => set({ setTab: fn }),

// Hub.tsx
useEffect(() => { registerSetTab((tab) => setActiveTab(tab as TabId)) }, [])

// Taverna.tsx
const setTab = useHubStore((s) => s.setTab)
setTab?.('deck')
```

**Proposed (direct):**
```ts
// hubStore
activeTab: 'home' as TabId,
setActiveTab: (tab: TabId) => set({ activeTab: tab }),

// Hub.tsx
const activeTab = useHubStore((s) => s.activeTab)

// Taverna.tsx
const setActiveTab = useHubStore((s) => s.setActiveTab)
setActiveTab('deck')
```

### 7.3 — `Taverna.tsx` direct store mutation

Line 38: `useHubStore.getState().setActiveDeck(deck)` — calling `getState()` from inside a component is a Zustand anti-pattern. After extracting `useTavernaData`, the hook should use the store setter obtained via `useHubStore((s) => s.setActiveDeck)`.

Same issue on lines 70-71 and 85 with `useMatchmakingStore.getState()`.

---

## 8. New Hooks Summary

| Hook | File | Extracted from | Priority |
|---|---|---|---|
| `useCardSelection` | `hooks/useCardSelection.ts` | `Game.tsx` | High |
| `useGameActions` | `hooks/useGameActions.ts` | `Game.tsx` | High |
| `useFlyingCard` | `hooks/useFlyingCard.ts` | `Game.tsx` | High |
| `useRevealedCards` | `hooks/useRevealedCards.ts` | `Game.tsx` | Low |
| `useErrorAutoDismiss` | `hooks/useErrorAutoDismiss.ts` | `Game.tsx` | Low |
| `useTavernaData` | `hooks/useTavernaData.ts` | `Taverna.tsx` | Medium |
| `useMatchmaking` | `hooks/useMatchmaking.ts` | `Taverna.tsx` | Medium |
| `useDeckEditor` | `hooks/useDeckEditor.ts` | `DeckForge.tsx` | Medium |
| `useMesaPrivada` | `hooks/useMesaPrivada.ts` | `MesaPrivadaModal.tsx` | Low |

---

## 9. Implementation Order

Work in this order to minimize conflicts and enable incremental testing:

### Phase 1 — Game.tsx (highest impact)

1. `useCardSelection` — extract first, no dependencies on other new hooks
2. `useFlyingCard` — self-contained animation logic
3. `useGameActions` — depends on card selection and flying card hooks
4. `useRevealedCards` + `useErrorAutoDismiss` — small, independent
5. Rewrite `Game.tsx` to compose all hooks

**Test:** play a full game end-to-end (play cards, pass, mulligan, medic, leader abilities, game over).

### Phase 2 — Hub pages

6. `useTavernaData` — extract deck fetching
7. `useMatchmaking` — extract matchmaking flow
8. Rewrite `Taverna.tsx`
9. `useDeckEditor` — extract full CRUD
10. Rewrite `DeckForge.tsx`

**Test:** create/edit/delete decks, search for opponent, play private match.

### Phase 3 — Minor cleanup

11. `useMesaPrivada` — extract from modal
12. Fix `hubStore.setTab` pattern (section 7.2)
13. Fix `getState()` calls (section 7.3)
14. Remove debug `console.log` (section 7.1)

**Test:** full regression — login, hub navigation, deck management, matchmaking, game flow.

---

## 10. Testing Checklist

After each phase, verify these flows work correctly:

- [ ] Login / Register
- [ ] Hub navigation (all 5 tabs)
- [ ] Deck creation (new deck, set faction, add/remove cards, set leader, save)
- [ ] Deck editing and deletion
- [ ] Deck picker in Taverna
- [ ] Matchmaking (search, cancel, found → navigate to game)
- [ ] Private match (create, share code, join)
- [ ] Resume active game banner
- [ ] Game: mulligan phase
- [ ] Game: play card (unit, hero, spy, weather, agile)
- [ ] Game: pass turn
- [ ] Game: leader ability (all variants)
- [ ] Game: medic revive
- [ ] Game: Scoia'tael first player choice
- [ ] Game: revealed cards overlay (Emperor of Nilfgaard)
- [ ] Game: flying card animation
- [ ] Game: card selection / board card inspection / leader inspection
- [ ] Game: turn countdown timer
- [ ] Game: opponent disconnect banner
- [ ] Game: round end overlay
- [ ] Game: game over screen → back to hub
- [ ] Game: surrender flow
- [ ] Game: error notifications (auto-dismiss)
