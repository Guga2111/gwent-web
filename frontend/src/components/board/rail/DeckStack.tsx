import CountBadge from '@/components/ui/CountBadge'

interface DeckStackProps {
  count: number
  label: string
}

export default function DeckStack({ count, label }: DeckStackProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
      <div style={{ position: 'relative', width: 50, height: 64 }}>
        {/* Stacked cards */}
        {[2, 1, 0].map((offset) => (
          <div
            key={offset}
            style={{
              position: 'absolute',
              top: offset * 2,
              left: offset * 2,
              width: 50 - offset * 4,
              height: 64 - offset * 4,
              backgroundColor: 'var(--bg-card)',
              border: '1px solid var(--border-gold)',
              borderRadius: 3,
            }}
          />
        ))}
        {/* Count badge */}
        <div style={{ position: 'absolute', top: -6, right: -6, zIndex: 3 }}>
          <CountBadge value={count} size={22} fontSize={10} />
        </div>
      </div>
      <div
        style={{
          fontSize: 9,
          fontFamily: 'var(--font-heading)',
          color: 'var(--text-muted)',
        }}
      >
        {label}
      </div>
    </div>
  )
}
