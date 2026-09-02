import { X } from 'lucide-react'
import type { DeckDto } from '@/types/deck'
import { getFactionConfig } from '@/utils/factionConfig'

interface Props {
  open: boolean
  decks: DeckDto[]
  activeDeckId: string | null
  onSelect: (deck: DeckDto) => void
  onClose: () => void
}

export default function DeckPickerModal({ open, decks, activeDeckId, onSelect, onClose }: Props) {
  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center backdrop-blur-sm"
      style={{ background: 'color-mix(in srgb, var(--bg-darkest) 84%, transparent)' }}
      onClick={onClose}
    >
      <div
        className="relative rounded-lg overflow-hidden"
        style={{
          width: 'min(460px, 92vw)',
          background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
          boxShadow: [
            'inset 0 0 0 2px color-mix(in srgb, var(--bg-darkest) 90%, transparent)',
            'inset 0 0 0 4px color-mix(in srgb, var(--gold) 30%, transparent)',
            '0 30px 80px rgba(0,0,0,.75)',
          ].join(', '),
          animation: 'gw-rise .3s ease',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 w-[30px] h-[30px] rounded-full flex items-center justify-center text-[var(--gold)] border-none"
          style={{
            background: 'color-mix(in srgb, var(--bg-darkest) 30%, transparent)',
            boxShadow: 'inset 0 0 0 1px color-mix(in srgb, var(--gold) 25%, transparent)',
          }}
        >
          <X size={15} strokeWidth={2.2} />
        </button>

        {/* Header */}
        <div className="px-[34px] pt-8 pb-5">
          <div
            className="text-[10.5px] tracking-[4px] uppercase font-bold text-[var(--gold)]"
            style={{ fontFamily: 'var(--font-heading)' }}
          >
            Escolher Baralho
          </div>
        </div>

        {/* Deck list */}
        <div className="overflow-y-auto px-[34px] pb-8 max-h-[60vh]">
          {decks.length === 0 ? (
            <p className="text-centertext-sm text-[var(--text-muted)] py-6">
              Nenhum baralho criado
            </p>
          ) : (
            <div className="flex flex-col gap-[5px]">
              {decks.map((deck) => {
                const cfg = getFactionConfig(deck.faction)
                const cardCount = deck.cards.reduce((s, e) => s + e.quantity, 0)
                const isActive = deck.id === activeDeckId

                return (
                  <button
                    key={deck.id}
                    onClick={() => onSelect(deck)}
                    className={`w-full flex items-center gap-3 px-4 py-[11px] rounded-[7px] border-none text-left transition-colors hover:bg-[var(--bg-hover)] ${isActive ? 'bg-[var(--bg-hover)]' : ''}`}
                    style={{
                      borderLeft: isActive ? '3px solid var(--gold)' : '3px solid transparent',
                    }}
                  >
                    {/* Faction color dot */}
                    <div
                      className="w-[10px] h-[10px] rounded-full flex-shrink-0"
                      style={{ background: `var(${cfg.accentColor})` }}
                    />

                    {/* Name + faction label */}
                    <div className="flex-1 min-w-0">
                      <div
                        className="font-bold text-[14px] text-[var(--text-primary)] truncate"
                        style={{ fontFamily: 'var(--font-heading)' }}
                      >
                        {deck.name}
                      </div>
                      <div className="text-[11.5px] text-[var(--text-muted)] mt-px">
                        {cfg.label}
                      </div>
                    </div>

                    {/* Card count badge */}
                    <div
                      className="flex-shrink-0 px-[9px] py-[3px] rounded-full text-[11.5px] font-bold text-[var(--gold)]"
                      style={{
                        background: 'color-mix(in srgb, var(--gold) 12%, transparent)',
                        boxShadow: 'inset 0 0 0 1px color-mix(in srgb, var(--gold) 25%, transparent)',
                        fontFamily: 'var(--font-heading)',
                      }}
                    >
                      {cardCount}
                    </div>
                  </button>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
