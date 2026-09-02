import { FACTIONS } from './DeckEditorHeader'
import type { Faction, CatalogCardDto, DeckCardEntryDto } from '@/types/deck'
import { CatalogCardItem } from './CatalogCardItem'

interface CatalogPanelProps {
  faction: Faction
  catalog: CatalogCardDto[]
  editorCards: DeckCardEntryDto[]
  onAdd: (card: CatalogCardDto) => void
}

export function CatalogPanel({ faction, catalog, editorCards, onAdd }: CatalogPanelProps) {
  const factionLabel = FACTIONS.find((f) => f.value === faction)?.label ?? faction

  return (
    <div className="flex-1 flex flex-col min-w-0 border-r deckforge-catalog-panel">
      <div className="px-4 py-2 text-[10px] font-bold tracking-[2px] uppercase text-gold border-b flex-shrink-0 deckforge-catalog-header">
        Catálogo · {factionLabel}
      </div>
      <div className="flex-1 overflow-y-auto">
        <div
          className="grid gap-3 p-3"
          style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(90px, 1fr))' }}
        >
          {catalog.map((card) => {
            const qty = editorCards.find((e) => e.cardId === card.id)?.quantity ?? 0
            return (
              <CatalogCardItem
                key={card.id}
                card={card}
                qty={qty}
                onAdd={() => onAdd(card)}
              />
            )
          })}
        </div>
      </div>
    </div>
  )
}
