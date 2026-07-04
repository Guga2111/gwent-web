interface LeaderCardProps {
  leaderUsed: boolean
  onClick: () => void
  disabled: boolean
  side?: 'top' | 'bottom'
}

export default function LeaderCard({ leaderUsed, onClick, disabled, side = 'bottom' }: LeaderCardProps) {
  const rotation = side === 'top' ? -4 : 4
  const isClickable = !disabled && !leaderUsed

  return (
    <div className="flex justify-center py-2">
      <div
        onClick={isClickable ? onClick : undefined}
        className="leader-card-base"
        style={{
          transform: `rotate(${rotation}deg)`,
          cursor: isClickable ? 'pointer' : 'default',
          opacity: leaderUsed ? 0.5 : 1,
          filter: leaderUsed ? 'saturate(0.3)' : undefined,
        }}
        onMouseEnter={(e) => {
          if (isClickable) {
            e.currentTarget.style.transform = `rotate(${rotation}deg) translateY(-4px)`
          }
        }}
        onMouseLeave={(e) => {
          if (isClickable) {
            e.currentTarget.style.transform = `rotate(${rotation}deg)`
          }
        }}
      >
        <div
          className="text-[9px] text-center text-[var(--gold-light)] px-1"
          style={{ fontFamily: 'var(--font-heading)' }}
        >
          Líder
        </div>
      </div>
    </div>
  )
}
