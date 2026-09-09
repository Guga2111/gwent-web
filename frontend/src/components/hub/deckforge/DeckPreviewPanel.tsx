import { X } from 'lucide-react'
import type { CatalogCardDto, DeckCardEntryDto } from '@/types/deck'
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Label } from '@/components/ui/label'

function totalCount(cards: DeckCardEntryDto[]) {
  return cards.reduce((s, e) => s + e.quantity, 0)
}

interface DeckPreviewPanelProps {
  leaders: CatalogCardDto[]
  leaderId: string
  cards: DeckCardEntryDto[]
  cardById: Record<string, CatalogCardDto>
  onLeaderChange: (id: string) => void
  onRemove: (cardId: string) => void
}

export function DeckPreviewPanel({ leaders, leaderId, cards, cardById, onLeaderChange, onRemove }: DeckPreviewPanelProps) {
  const count = totalCount(cards)

  return (
    <div className="w-[320px] flex-shrink-0 flex flex-col deckforge-deck-panel">
      <div className="px-4 py-3 border-b deckforge-section-border">
        <Select value={leaderId || undefined} onValueChange={onLeaderChange}>
          <SelectTrigger className="font-ui w-full px-2 py-1.5 rounded text-sm text-text-primary bg-bg-darkest border-none outline-none cursor-pointer">
            <SelectValue placeholder="Selecione um líder" />
          </SelectTrigger>
          <SelectContent position="popper" className="bg-bg-dark border-gold/30">
            <SelectGroup>
              <SelectLabel>Líderes</SelectLabel>
              {leaders.map((l) => (
                <SelectItem key={l.id} value={l.id}>{l.name}</SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div className="px-4 py-2 border-b flex items-center gap-2 deckforge-section-border">
        <Label className="text-xs font-bold tracking-[2px] uppercase text-gold">Cartas</Label>
        <Label className={`text-sm font-bold ${count < 22 || count > 40 ? 'text-red' : 'text-green'}`}>
          {count}
        </Label>
        <Label className="text-xs text-text-muted">/ 22–40</Label>
      </div>

      <div className="flex-1 overflow-y-auto p-3">
        {cards.length === 0 && (
          <Label className="font-body text-center text-sm text-text-muted mt-4">
            Clique nas cartas do catálogo para adicioná-las
          </Label>
        )}
        {cards.map((entry) => {
          const card = cardById[entry.cardId]
          return (
            <div key={entry.cardId} className="flex items-center gap-2 py-1.5 border-b deckforge-card-list-item">
              <span className="text-xs font-bold text-gold w-5 text-center">{entry.quantity}×</span>
              <span className="flex-1 text-sm text-text-primary truncate">{card?.name ?? entry.cardId}</span>
              <button
                onClick={() => onRemove(entry.cardId)}
                className="bg-transparent border-none cursor-pointer text-text-muted flex-shrink-0 hover:text-white"
              >
                <X size={12} />
              </button>
            </div>
          )
        })}
      </div>
    </div>
  )
}
