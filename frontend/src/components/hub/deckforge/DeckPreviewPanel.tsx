import { X } from 'lucide-react'
import type { CatalogCardDto, DeckCardEntryDto } from '@/types/deck'

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
        <div className="text-[10px] font-bold tracking-[2px] uppercase text-[var(--gold)] mb-2">Líder</div>
        <select
          value={leaderId}
          onChange={(e) => onLeaderChange(e.target.value)}
          className="font-ui deckforge-leader-select w-full px-2 py-1.5 rounded text-[12px] text-[var(--text-primary)] bg-[var(--bg-darkest)] border-none outline-none cursor-pointer"
        >
          <option value="">Selecione um líder</option>
          {leaders.map((l) => (
            <option key={l.id} value={l.id}>{l.name}</option>
          ))}
        </select>
      </div>

      <div className="px-4 py-2 border-b flex items-center gap-2 deckforge-section-border">
        <span className="text-[10px] font-bold tracking-[2px] uppercase text-[var(--gold)]">Cartas</span>
        <span className={`text-[13px] font-bold ${count < 22 || count > 40 ? 'text-[var(--red)]' : 'text-[var(--green)]'}`}>
          {count}
        </span>
        <span className="text-[11px] text-[var(--text-muted)]">/ 22–40</span>
      </div>

      <div className="flex-1 overflow-y-auto p-3">
        {cards.length === 0 && (
          <p className="font-body text-center text-[12px] italic text-[var(--text-muted)] mt-4">
            Clique nas cartas do catálogo para adicioná-las
          </p>
        )}
        {cards.map((entry) => {
          const card = cardById[entry.cardId]
          return (
            <div key={entry.cardId} className="flex items-center gap-2 py-1.5 border-b deckforge-card-list-item">
              <span className="text-[11px] font-bold text-[var(--gold)] w-5 text-center">{entry.quantity}×</span>
              <span className="flex-1 text-[12px] text-[var(--text-primary)] truncate">{card?.name ?? entry.cardId}</span>
              <button
                onClick={() => onRemove(entry.cardId)}
                className="bg-transparent border-none cursor-pointer text-[var(--text-muted)] flex-shrink-0"
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
