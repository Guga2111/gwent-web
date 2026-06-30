interface BoardCardProps {
  cardId: string
  onClick?: () => void
  interactive: boolean
}

export default function BoardCard({ cardId, onClick, interactive }: BoardCardProps) {
  return (
    <div
      onClick={interactive ? onClick : undefined}
      style={{
        width: 56,
        height: 82,
        backgroundColor: 'var(--bg-card)',
        border: '1px solid var(--border-gold)',
        borderRadius: 4,
        position: 'relative',
        flexShrink: 0,
        cursor: interactive ? 'pointer' : 'default',
        transition: 'transform 0.15s, border-color 0.15s',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
      onMouseEnter={(e) => {
        if (interactive) {
          e.currentTarget.style.transform = 'translateY(-6px)'
          e.currentTarget.style.borderColor = 'var(--gold-light)'
        }
      }}
      onMouseLeave={(e) => {
        if (interactive) {
          e.currentTarget.style.transform = 'translateY(0)'
          e.currentTarget.style.borderColor = 'var(--border-gold)'
        }
      }}
    >
      {/* Card ID label */}
      <div
        style={{
          fontSize: 8,
          fontFamily: 'var(--font-heading)',
          color: 'var(--text-secondary)',
          textAlign: 'center',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          padding: '0 4px',
          maxWidth: '100%',
        }}
      >
        {cardId}
      </div>
    </div>
  )
}
