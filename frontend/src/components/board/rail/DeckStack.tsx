import CountBadge from '@/components/ui/CountBadge'

interface DeckStackProps {
  count: number
  label: string
}

export default function DeckStack({ count, label }: DeckStackProps) {
  return (
    <div className="flex flex-col items-center gap-1">
      <div className="relative w-[50px] h-16">
        {/* Stacked cards */}
        {[2, 1, 0].map((offset) => (
          <div
            key={offset}
            className="absolute bg-[var(--bg-card)] border border-[var(--border-gold)] rounded-sm"
            style={{
              top: offset * 2,
              left: offset * 2,
              width: 50 - offset * 4,
              height: 64 - offset * 4,
            }}
          />
        ))}
        {/* Count badge */}
        <div className="absolute -top-1.5 -right-1.5 z-30">
          <CountBadge value={count} size={22} fontSize={10} />
        </div>
      </div>
      <div className="label-muted">{label}</div>
    </div>
  )
}
