import type { CatalogCardDto } from '@/types/deck'
import CardArtImage from '@/components/board/card/CardArtImage'
import PowerGem from '@/components/board/card/PowerGem'
import AbilityIcon from '@/components/board/card/AbilityIcon'
import RowIcon from '@/components/board/card/RowIcon'
import { Sword, BowArrow } from 'lucide-react'
import { getFactionConfig } from '@/utils/factionConfig'
import { Label } from '@/components/ui/label'

interface CatalogCardItemProps {
  card: CatalogCardDto
  qty: number
  onAdd: () => void
}

export function CatalogCardItem({ card, onAdd, qty }: CatalogCardItemProps) {
  const maxCopies = card.cardType === 'HERO' ? 1 : card.deckCopies
  const atMax = qty >= maxCopies
  const isHero = card.cardType === 'HERO'

  const { tokens } = getFactionConfig(card.faction as any)
  const artStyle = tokens
    ? { background: `linear-gradient(160deg, ${tokens.secondary} 0%, color-mix(in srgb, ${tokens.primary} 25%, ${tokens.secondary}) 50%, ${tokens.secondary} 100%)` }
    : undefined

  const wrapperClass = [
    'flex flex-col items-center cursor-pointer',
    atMax && 'opacity-50 pointer-events-none',
  ]
    .filter(Boolean)
    .join(' ')

  const cardClass = [
    'card-base card-face shrink-0 flex items-center justify-center',
    isHero && 'card-face--hero',
    qty > 0 && 'deckforge-catalog-card--selected',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={wrapperClass} onClick={onAdd}>
      <div className={cardClass} style={{ width: '100%', height: 'auto', aspectRatio: '72 / 106' }}>
        <div className="card-art" style={artStyle}>
          <CardArtImage cardId={card.id} faction={card.faction} />
        </div>
        {card.basePower != null && (
          <PowerGem basePower={card.basePower} currentPower={card.basePower} isHero={isHero} />
        )}
        {card.ability && card.ability !== 'NONE' && (
          <AbilityIcon ability={card.ability as any} />
        )}
        {card.rowType && card.rowType !== 'AGILE' && (
          <RowIcon rowType={card.rowType as 'MELEE' | 'RANGED' | 'SIEGE'} />
        )}
        {card.rowType === 'AGILE' && (
          <div className="absolute top-[26px] left-[6px] flex flex-col gap-0.5 text-text-muted">
            <Sword size={14} strokeWidth={2} />
            <BowArrow size={14} strokeWidth={2} />
          </div>
        )}
      </div>
      <Label className="text-sm text-center text-text-primary truncate w-full mt-1">
        {card.name}
      </Label>
      <Label className="text-xs text-center text-muted-foreground">
        {qty > 0 && <span className="text-gold font-bold">{qty}/</span>}
        {maxCopies}
      </Label>
    </div>
  )
}
