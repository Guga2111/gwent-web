import { Plus, Trash2 } from 'lucide-react'
import type { DeckDto, DeckCardEntryDto } from '@/types/deck'

function totalCount(cards: DeckCardEntryDto[]) {
  return cards.reduce((s, e) => s + e.quantity, 0)
}

interface DeckListSidebarProps {
  decks: DeckDto[]
  onNew: () => void
  onEdit: (d: DeckDto) => void
  onDelete: (id: string) => void
}

export function DeckListSidebar({ decks, onNew, onEdit, onDelete }: DeckListSidebarProps) {
  return (
    <div className="w-[280px] flex-shrink-0 flex flex-col border-r deckforge-sidebar">
      <div className="flex items-center justify-between px-5 py-4 border-b deckforge-sidebar-header">
        <span className="font-heading font-bold text-[11px] tracking-[2.5px] uppercase text-gold">
          Meus Baralhos
        </span>
        <button
          onClick={onNew}
          className="flex items-center gap-1.5 px-3 py-1 rounded border-none cursor-pointer text-[11px] font-semibold text-bg-darkest btn-gold"
        >
          <Plus size={12} strokeWidth={2.5} />
          Novo
        </button>
      </div>
      <div className="flex-1 overflow-y-auto">
        {decks.length === 0 && (
          <div className="px-5 py-8 text-center">
            <p className="font-bodytext-[13px] text-text-muted">
              Nenhum baralho ainda
            </p>
            <button
              onClick={onNew}
              className="mt-3 text-[12px] underline text-gold bg-transparent border-none cursor-pointer"
            >
              Criar agora
            </button>
          </div>
        )}
        {decks.map((d) => (
          <div
            key={d.id}
            className="flex items-center gap-2 px-4 py-3 border-b cursor-pointer deckforge-deck-item"
            onClick={() => onEdit(d)}
          >
            <div className="flex-1 min-w-0">
              <div className="font-semibold text-[13px] text-text-primary truncate">{d.name}</div>
              <div className="text-[11px] text-text-muted">
                {totalCount(d.cards)} cartas
              </div>
            </div>
            <button
              onClick={(ev) => { ev.stopPropagation(); onDelete(d.id) }}
              className="flex-shrink-0 bg-transparent border-none cursor-pointer text-text-muted hover:text-red"
            >
              <Trash2 size={13} />
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
