interface ControlBarProps {
  onSurrender: () => void
}

export default function ControlBar({ onSurrender }: ControlBarProps) {
  return (
    <div
      style={{
        position: 'absolute',
        bottom: 0,
        left: 0,
        right: 0,
        height: 48,
        background: 'linear-gradient(transparent, var(--bg-darkest))',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'flex-end',
        gap: 12,
        paddingBottom: 8,
        pointerEvents: 'none',
      }}
    >
      {[
        { label: 'Desistir', icon: '🏳', action: onSurrender },
        { label: 'Ampliar', icon: '🔍', action: () => {} },
        { label: 'Selecionar', icon: '↵', action: () => {} },
      ].map(({ label, icon, action }) => (
        <button
          key={label}
          onClick={action}
          style={{
            pointerEvents: 'auto',
            backgroundColor: 'var(--bg-medium)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 4,
            padding: '4px 10px',
            fontSize: 10,
            fontFamily: 'var(--font-ui)',
            color: 'var(--text-muted)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 4,
          }}
        >
          <span>{icon}</span>
          {label}
        </button>
      ))}
    </div>
  )
}
