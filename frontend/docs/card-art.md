 # Card Art Images

Card art images live in `frontend/public/cards/`, organized by faction subfolder.

## Folder Structure

```
frontend/public/cards/
├── neutral/       ← NEUTRAL_ prefix
├── northern/      ← NR_ prefix
├── nilfgaard/     ← NG_ prefix
├── scoiatael/     ← ST_ prefix
├── monster/       ← MO_ prefix
└── skellige/      ← SK_ prefix
```

## File Naming

Each file uses the card's **catalog ID** as filename, in WebP format:

```
{FACTION_PREFIX}_{CARD_NAME}.webp
```

Examples:

| Card Name | Faction | Filename | Full Path |
|-----------|---------|----------|-----------|
| Geralt of Rivia | Neutral | `NEUTRAL_HERO_GERALT.webp` | `neutral/NEUTRAL_HERO_GERALT.webp` |
| Blue Stripes | Northern Realms | `NR_BLUE_STRIPES.webp` | `northern/NR_BLUE_STRIPES.webp` |
| Yennefer | Neutral | `NEUTRAL_HERO_YENNEFER.webp` | `neutral/NEUTRAL_HERO_YENNEFER.webp` |
| Foltest | Northern Realms | `NR_LEADER_FOLTEST.webp` | `northern/NR_LEADER_FOLTEST.webp` |

Leaders live in their faction folder, prefixed with `_LEADER_`.

## Multi-Copy Cards

Cards that appear multiple times in a deck receive suffixed instance IDs at runtime (e.g., `NR_BLUE_STRIPES_1`, `NR_BLUE_STRIPES_2`). The suffix is automatically stripped when resolving the image — all copies share the same art file.

## Fallback Behavior

If an image file is missing, the card renders its faction-colored gradient as before. No broken image icon is shown — the `<img>` element is removed from the DOM on load error.

## Adding New Art

1. Find the card's catalog ID in the backend catalog (`backend/gwent-engine/src/main/resources/cards/`)
2. Name the WebP file using that exact ID
3. Drop it in the matching faction subfolder
4. The card will pick it up automatically — no code changes needed
