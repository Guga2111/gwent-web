import CountBadge from '@/components/ui/CountBadge'
import CardBack from '../card/CardBack'
import type { Faction } from '@/types/game'

interface DeckStackProps {
  count: number
  label: string
  faction: Faction
}

export default function DeckStack({ count, label, faction }: DeckStackProps) {
  return (
    <div className="flex flex-col items-center gap-1">
      <div className="relative w-(--card-w) h-(--card-h)">
        {/* Depth layers — stacked cards behind */}
        {count > 2 && (
          <div className="stack-depth-layer" style={{ top: 3, left: 3 }} />
        )}
        {count > 1 && (
          <div className="stack-depth-layer" style={{ top: 1.5, left: 1.5 }} />
        )}
        {/* Main card */}
        <div className="relative z-10">
          <CardBack faction={faction} />
        </div>
        {/* Count badge */}
        <div className="absolute -top-1.5 -right-1.5 z-30">
          <CountBadge value={count} size={27} fontSize={14} />
        </div>
      </div>
      <div className="text-[9px] text-text-muted font-heading">{label}</div>
    </div>
  )
}
