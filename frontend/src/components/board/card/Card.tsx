import type { CardDto, Faction } from '@/types/game'
import CardBack from './CardBack'
import { factionTokens } from './CardBack'
import CardArtImage from './CardArtImage'
import PowerGem from './PowerGem'
import RowIcon from './RowIcon'
import AbilityIcon from './AbilityIcon'

interface CardProps {
  card?: CardDto        // present = face-up, absent = face-down
  onClick?: () => void
  interactive?: boolean
  faction?: Faction
  suppressEnterAnimation?: boolean
}

export default function Card({ card, onClick, interactive = false, faction, suppressEnterAnimation }: CardProps) {
  if (!card) {
    return <CardBack faction={faction} />
  }

  const isHero = card.cardType === 'HERO'
  const isUnit = card.cardType === 'UNIT' || isHero
  const hasPower = isUnit && card.basePower != null

  const tokens = factionTokens[card.faction]
  const artStyle = tokens
    ? { background: `linear-gradient(160deg, ${tokens.secondary} 0%, color-mix(in srgb, ${tokens.primary} 25%, ${tokens.secondary}) 50%, ${tokens.secondary} 100%)` }
    : undefined

  const className = [
    'card-base card-face shrink-0 flex items-center justify-center',
    isHero && 'card-face--hero',
    interactive && 'card-face--interactive',
    !interactive && 'cursor-default',
    suppressEnterAnimation && 'card-enter--suppressed',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <div
      onClick={interactive ? onClick : undefined}
      className={className}
    >
      {/* Background art / faction gradient */}
      <div className="card-art" style={artStyle}>
        <CardArtImage cardId={card.id} faction={card.faction} />
      </div>

      {/* Power gem */}
      {hasPower && (
        <PowerGem
          basePower={card.basePower!}
          currentPower={card.currentPower ?? card.basePower!}
          isHero={isHero}
        />
      )}

      {/* Row icon */}
      {isUnit && card.rowType && <RowIcon rowType={card.rowType} />}

      {/* Ability icon */}
      {card.ability && card.ability !== 'NONE' && (
        <AbilityIcon ability={card.ability} />
      )}

    </div>
  )
}
