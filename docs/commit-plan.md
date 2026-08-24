# Commit Plan — `board-pooling` branch

9 ordered commits. Execute each block in sequence — the project should compile after every commit.

---

## Commit 1 — Backend fixes (score calc + weather card copies)

```bash
git add \
  backend/gwent-engine/src/main/java/com/gwent/engine/core/ScoreCalculator.java \
  backend/gwent-api/src/main/java/com/gwent/api/catalog/CardCatalogLoader.java

git commit -m "$(cat <<'EOF'
fix: remove non-existent leaderBonusPower reference and enable weather cards in decks

Remove two calls to row.getLeaderBonusPower() that referenced a method
that no longer exists. Change weather card deckCopies from 0 to 3 so
they are actually included in game decks.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 2 — Backend DTO: add leaderAbility to CardDto

```bash
git add \
  backend/gwent-api/src/main/java/com/gwent/api/game/dto/CardDto.java \
  backend/gwent-api/src/main/java/com/gwent/api/game/GameModelMapper.java

git commit -m "$(cat <<'EOF'
feat: expose leaderAbility field in CardDto

Add leaderAbility (String, nullable) to the CardDto record so the
frontend can display leader ability names and descriptions.
GameModelMapper now maps the leader ability for leader cards and
passes null for regular hand cards.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 3 — Frontend types + .gitignore

```bash
git add \
  frontend/src/types/game.ts \
  .gitignore

git commit -m "$(cat <<'EOF'
feat: update frontend type definitions and add LeaderAbility type

- Correct Faction: MONSTERS -> MONSTER
- Rename abilities: BOND -> TIGHT_BOND, MORALE -> MORALE_BOOST,
  DECOY -> DUMMY, HORN -> COMMANDERS_HORN
- Add weather abilities: FROST, FOG, RAIN, CLEAR_WEATHER
- Add LeaderAbility union type with all 22 leader abilities
- Add leaderAbility field to CardDto interface
- Update .gitignore: frontend/src/assets/* -> frontend/public/cards/*

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 4 — CSS: board surface, card visuals, and component styles

```bash
git add \
  frontend/src/styles/index.css

git commit -m "$(cat <<'EOF'
feat: add complete visual system — board surface, card faces, and component styles

Add ~1000 lines of CSS covering:
- Faction color palette variables and responsive card dimensions
- Board surface textures (game-table wood grain, board-mat leather)
- Card face/back/art styles, hero glow, interactive hover states
- Power gem, ability/row icon positioning
- Board row tinting, weather overlays, horn slot, score flash
- Central divider medallion design
- Player panel, pass button, hand fan layout
- Flying card animation and card detail panel
- Weather slot targeting highlight
- Tooltip system via data-tooltip attribute

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 5 — New leaf components (no cross-imports between new files)

```bash
git add \
  frontend/src/components/board/card/cardArt.ts \
  frontend/src/components/board/card/PowerGem.tsx \
  frontend/src/components/board/controls/SurrenderDialog.tsx \
  frontend/docs/card-art.md

git commit -m "$(cat <<'EOF'
feat: add card art utility, PowerGem component, and SurrenderDialog

- cardArt.ts: utility to resolve card art image URLs by faction/id
- PowerGem.tsx: displays card power in a gem with hero/buff/debuff
  color states
- SurrenderDialog.tsx: modal confirmation dialog before surrendering
- card-art.md: documentation for the card art asset pipeline

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 6 — New components that depend on types/cardArt (CardBack, icons, art image)

```bash
git add \
  frontend/src/components/board/card/CardBack.tsx \
  frontend/src/components/board/card/CardArtImage.tsx \
  frontend/src/components/board/card/AbilityIcon.tsx \
  frontend/src/components/board/card/RowIcon.tsx

git commit -m "$(cat <<'EOF'
feat: add CardBack, CardArtImage, AbilityIcon, and RowIcon components

- CardBack: faction-colored card reverse with SVG emblems, corner
  ornaments, and exported factionTokens/emblems maps
- CardArtImage: loads card art PNGs from /public/cards/{faction}/
  with graceful error fallback
- AbilityIcon: maps each Ability to a Lucide icon (SPY->Eye,
  TIGHT_BOND->Link, MORALE_BOOST->ChevronUp, etc.)
- RowIcon: custom SVG icons for MELEE (sword), RANGED (bow),
  SIEGE (catapult) row types

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 7 — Card.tsx refactor + CardDetailPanel + FlyingCard

```bash
git add \
  frontend/src/components/board/card/Card.tsx \
  frontend/src/components/board/card/CardDetailPanel.tsx \
  frontend/src/components/board/card/FlyingCard.tsx

git commit -m "$(cat <<'EOF'
refactor: redesign Card component and add CardDetailPanel + FlyingCard

Card.tsx — complete visual overhaul:
- Faction-colored gradient background with card art overlay
- PowerGem, RowIcon, AbilityIcon sub-components
- Hero card golden border glow
- suppressEnterAnimation prop for row-entry control

CardDetailPanel — expandable card inspector in the right rail:
- Large art area with power gem and row/ability icons
- Card name, type label, ability/leader descriptions in Portuguese
- Optional action button (e.g. "Usar Habilidade" for leaders)

FlyingCard — portal-rendered card animation from hand to board row

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 8 — Rail, row, and control component updates

```bash
git add \
  frontend/src/components/board/card/LeaderCard.tsx \
  frontend/src/components/board/controls/ControlBar.tsx \
  frontend/src/components/board/rail/DeckStack.tsx \
  frontend/src/components/board/rail/GraveyardStack.tsx \
  frontend/src/components/board/rail/PassButton.tsx \
  frontend/src/components/board/rail/PlayerPanel.tsx \
  frontend/src/components/board/rail/WeatherZone.tsx \
  frontend/src/components/board/row/BoardRow.tsx \
  frontend/src/components/board/row/CentralDivider.tsx \
  frontend/src/components/board/row/Hand.tsx

git commit -m "$(cat <<'EOF'
refactor: update rail, row, and control components for new visual system

LeaderCard: show leader name and card art, always clickable
ControlBar: surrender button with SurrenderDialog + confirm-play button
DeckStack: faction-colored CardBack with depth layers
GraveyardStack: card-sized stack with count badge
PassButton: use pass-button CSS class
PlayerPanel: horizontal layout with faction emblem, username, hand
  count, lives gems, and score badge
WeatherZone: interactive targeting for weather card placement
BoardRow: score flash animation, card inspection click handler,
  CSS-based row tinting and weather overlays, data attributes for
  flying card animation
CentralDivider: CSS-based medallion design
Hand: faction-colored card backs for opponent, departing card support,
  fan rotation with CSS classes

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Commit 9 — Game page integration

```bash
git add \
  frontend/src/pages/Game.tsx

git commit -m "$(cat <<'EOF'
feat: integrate card inspection, flying animation, and weather targeting into Game page

- Card inspection: click board cards or leaders to show CardDetailPanel
  in right rail; leader panel includes "Usar Habilidade" action button
- Flying card animation: cards animate from hand to target row via
  portal-rendered FlyingCard; syncs with WebSocket state updates
- Weather card targeting: selecting a weather card highlights the
  weather zone slots; clicking a slot plays the card
- Confirm-play button: ControlBar wired to play selected card directly
  for non-agile unit/weather cards
- Layout: flex-based board with game-table background, board-mat
  playing surface, and reorganized right rail (deck/graveyard pairs
  top and bottom, detail panel or controls in center)

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```
