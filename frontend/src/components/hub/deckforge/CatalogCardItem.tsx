import type { CatalogCardDto } from '@/types/deck'
import CardArtImage from '@/components/board/card/CardArtImage'
import PowerGem from '@/components/board/card/PowerGem'
import AbilityIcon from '@/components/board/card/AbilityIcon'
import { factionTokens } from '@/components/board/card/CardBack'

interface CatalogCardItemProps {
  card: CatalogCardDto
  qty: number
  onAdd: () => void
}

export function CatalogCardItem({ card, onAdd, qty }: CatalogCardItemProps) {
  const maxCopies = card.cardType === 'HERO' ? 1 : card.deckCopies
  const atMax = qty >= maxCopies
  const isHero = card.cardType === 'HERO'

  const tokens = factionTokens[card.faction as keyof typeof factionTokens]
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
    'card-base card-face',
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
      </div>
      <p className="text-xs text-center text-[var(--text-primary)] truncate w-full mt-1">
        {card.name}
      </p>
      <p className="text-[11px] text-center text-[var(--text-muted)]">
        {qty > 0 && <span className="text-[var(--gold)] font-bold">{qty}/</span>}
        {maxCopies}
      </p>
    </div>
  )
}
