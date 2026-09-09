import type { DeckDto } from '@/types/deck'
import { getFactionConfig } from '@/utils/factionConfig'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import {
  Choicebox,
  ChoiceboxItem,
  ChoiceboxItemHeader,
  ChoiceboxItemTitle,
  ChoiceboxItemSubtitle,
  ChoiceboxItemContent,
  ChoiceboxItemIndicator,
} from '@/components/ui/choicebox'

interface Props {
  open: boolean
  decks: DeckDto[]
  activeDeckId: string | null
  onSelect: (deck: DeckDto) => void
  onClose: () => void
}

export default function DeckPickerModal({ open, decks, activeDeckId, onSelect, onClose }: Props) {
  const deckMap = new Map(decks.map((d) => [d.id, d]))

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="mq-panel border-none max-w-[460px]">
        <DialogHeader>
          <DialogTitle className="text-[10.5px] tracking-[4px] uppercase font-bold text-gold font-heading">
            Escolher Baralho
          </DialogTitle>
          <DialogDescription className="sr-only">
            Selecione um baralho para usar na partida
          </DialogDescription>
        </DialogHeader>

        {decks.length === 0 ? (
          <p className="text-center text-sm text-text-muted py-6">
            Nenhum baralho criado
          </p>
        ) : (
          <Choicebox
            value={activeDeckId ?? undefined}
            onValueChange={(id) => {
              const deck = deckMap.get(id)
              if (deck) onSelect(deck)
            }}
            className="max-h-[60vh] overflow-y-auto space-y-[5px]"
          >
            {decks.map((deck) => {
              const cfg = getFactionConfig(deck.faction)
              const cardCount = deck.cards.reduce((s, e) => s + e.quantity, 0)

              return (
                <ChoiceboxItem
                  key={deck.id}
                  value={deck.id}
                  className="border-gold/10 bg-transparent hover:bg-bg-medium/50 transition-colors [&[data-state=checked]]:border-gold [&[data-state=checked]]:bg-bg-medium/50"
                >
                  <ChoiceboxItemHeader>
                    <ChoiceboxItemTitle className="text-[14px] text-text-primary font-heading font-bold gap-3">
                      <div
                        className={`size-2.5 rounded-full flex-shrink-0 bg-${cfg.accentColor}`}
                      />
                      {deck.name}
                    </ChoiceboxItemTitle>
                    <ChoiceboxItemSubtitle className="text-[11.5px] text-text-muted">
                      {cfg.label} · {cardCount} cartas
                    </ChoiceboxItemSubtitle>
                  </ChoiceboxItemHeader>
                  <ChoiceboxItemContent className="border-gold/25 text-gold">
                    <ChoiceboxItemIndicator className="fill-gold" />
                  </ChoiceboxItemContent>
                </ChoiceboxItem>
              )
            })}
          </Choicebox>
        )}
      </DialogContent>
    </Dialog>
  )
}
