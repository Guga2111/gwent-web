import type { CardDto } from '@/types/game'

interface CardProps {
  card?: CardDto        // present = face-up, absent = face-down
  onClick?: () => void
  interactive?: boolean
}

export default function Card({ card, onClick, interactive = false }: CardProps) {
  if (!card) {
    return (
      <div
        className="card-base"
        style={{
          backgroundImage:
            'repeating-linear-gradient(45deg, transparent, transparent 8px, rgba(106, 85, 48, 0.15) 8px, rgba(106, 85, 48, 0.15) 9px), repeating-linear-gradient(-45deg, transparent, transparent 8px, rgba(106, 85, 48, 0.15) 8px, rgba(106, 85, 48, 0.15) 9px)',
        }}
      />
    )
  }

  return (
    <div
      onClick={interactive ? onClick : undefined}
      className={`card-base relative flex-col gap-0.5 p-1 transition-[transform,border-color] duration-150 ${
        interactive
          ? 'cursor-pointer hover:-translate-y-1.5 hover:border-[var(--gold-light)]'
          : 'cursor-default'
      }`}
    >
      {card.basePower != null && (
        <div
          className="text-sm leading-none text-[var(--gold-light)]"
          style={{ fontFamily: 'var(--font-heading)' }}
        >
          {card.basePower}
        </div>
      )}
      <div
        className="text-[8px] text-center text-[var(--text-secondary)] overflow-hidden text-ellipsis whitespace-nowrap max-w-full"
        style={{ fontFamily: 'var(--font-heading)' }}
      >
        {card.name}
      </div>
    </div>
  )
}
