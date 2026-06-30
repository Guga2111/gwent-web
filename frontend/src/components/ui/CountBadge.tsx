interface CountBadgeProps {
  value: number
  size: number
  fontSize?: number
  bg?: string
  color?: string
}

export default function CountBadge({
  value,
  size,
  fontSize,
  bg = 'var(--bg-dark)',
  color = 'var(--gold-light)',
}: CountBadgeProps) {
  return (
    <div
      style={{
        width: size,
        height: size,
        borderRadius: '50%',
        backgroundColor: bg,
        border: '1px solid var(--border-gold)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontFamily: 'var(--font-heading)',
        fontSize: fontSize ?? Math.round(size * 0.35),
        fontWeight: 700,
        color,
        flexShrink: 0,
      }}
    >
      {value}
    </div>
  )
}
