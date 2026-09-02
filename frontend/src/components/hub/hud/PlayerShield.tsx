interface PlayerShieldProps {
  level?: number
  size?: 'sm' | 'md'
}

const sizes = {
  sm: { outer: [52, 64] as [number, number], inner: [42, 54] as [number, number] },
  md: { outer: [90, 106] as [number, number], inner: [76, 92] as [number, number] },
}

export default function PlayerShield({ level, size = 'sm' }: PlayerShieldProps) {
  const { outer, inner } = sizes[size]

  return (
    <div style={{ position: 'relative', flexShrink: 0 }}>
      <div
        style={{
          width: outer[0],
          height: outer[1],
          background: 'linear-gradient(180deg, var(--gold-light), var(--gold) 60%, var(--gold-dim))',
          clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          filter: 'drop-shadow(0 4px 8px rgba(0,0,0,.5))',
        }}
      >
        <div
          style={{
            width: inner[0],
            height: inner[1],
            clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
            background: 'var(--bg-dark)',
          }}
        />
      </div>
      {level !== undefined && (
        <div
          className="font-heading"
          style={{
            position: 'absolute',
            bottom: -6,
            left: '50%',
            transform: 'translateX(-50%)',
            background: 'linear-gradient(180deg, var(--gold-light), var(--gold-dark))',
            color: 'var(--bg-darkest)',
            fontWeight: 700,
            fontSize: '10.5px',
            padding: '1px 8px',
            borderRadius: 9,
            boxShadow: '0 2px 4px rgba(0,0,0,.5)',
            whiteSpace: 'nowrap',
          }}
        >
          {level}
        </div>
      )}
    </div>
  )
}
