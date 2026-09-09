import { useState, type ReactNode } from 'react'
import { Sword, BowArrow } from 'lucide-react'
import { IconWand } from '@tabler/icons-react'
import { SiegeIcon } from '@/components/board/card/RowIcon'
import type { Faction, CatalogCardDto, DeckCardEntryDto } from '@/types/deck'
import { CatalogCardItem } from './CatalogCardItem'
import { Button } from '@/components/ui/button'

type RowFilter = 'ALL' | 'MELEE' | 'RANGED' | 'SIEGE' | 'SPECIAL'

const ICON_SIZE = 12

const ROW_FILTERS: { value: RowFilter; label: string; icon?: ReactNode }[] = [
  { value: 'ALL', label: 'Todas' },
  { value: 'MELEE', label: 'Corpo a corpo', icon: <Sword size={ICON_SIZE} strokeWidth={2} /> },
  { value: 'RANGED', label: 'À distância', icon: <BowArrow size={ICON_SIZE} strokeWidth={2} /> },
  { value: 'SIEGE', label: 'Cerco', icon: <SiegeIcon size={ICON_SIZE} strokeWidth={2} /> },
  { value: 'SPECIAL', label: 'Especial', icon: <IconWand size={ICON_SIZE} stroke={2} /> },
]

interface CatalogPanelProps {
  faction: Faction
  catalog: CatalogCardDto[]
  editorCards: DeckCardEntryDto[]
  onAdd: (card: CatalogCardDto) => void
}

export function CatalogPanel({ catalog, editorCards, onAdd }: CatalogPanelProps) {
  const [rowFilter, setRowFilter] = useState<RowFilter>('ALL')

  const filtered = catalog.filter((card) => {
    if (rowFilter === 'ALL') return true
    if (rowFilter === 'SPECIAL') return card.cardType === 'SPECIAL' || card.cardType === 'WEATHER'
    return card.rowType === rowFilter
  })

  return (
    <div className="flex-1 flex flex-col min-w-0 border-r deckforge-catalog-panel">
      <div className="flex gap-1.5 px-3 py-2 flex-shrink-0">
        {ROW_FILTERS.map((f) => (
          <Button
            key={f.value}
            onClick={() => setRowFilter(f.value)}
            variant={rowFilter === f.value ? 'cta' : 'ghost'}
            size="pill"
            className="flex items-center gap-1 tracking-wide cursor-pointer"
          >
            {f.icon}
            {f.label}
          </Button>
        ))}
      </div>
      <div className="flex-1 overflow-y-auto">
        <div
          className="grid gap-4 p-3"
          style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(90px, 1fr))' }}
        >
          {filtered.map((card) => {
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
