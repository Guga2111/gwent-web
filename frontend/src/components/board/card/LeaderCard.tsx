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
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        padding: '8px 0',
      }}
    >
      <div
        onClick={isClickable ? onClick : undefined}
        style={{
          width: 70,
          height: 92,
          backgroundColor: 'var(--bg-card)',
          border: '1px solid var(--border-gold)',
          borderRadius: 4,
          transform: `rotate(${rotation}deg)`,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 4,
          cursor: isClickable ? 'pointer' : 'default',
          opacity: leaderUsed ? 0.5 : 1,
          filter: leaderUsed ? 'saturate(0.3)' : undefined,
          transition: 'transform 0.15s',
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
          style={{
            fontSize: 9,
            fontFamily: 'var(--font-heading)',
            color: 'var(--gold-light)',
            textAlign: 'center',
            padding: '0 4px',
          }}
        >
          Líder
        </div>
      </div>
    </div>
  )
}
