export default function CardBack() {
  return (
    <div
      style={{
        width: 56,
        height: 82,
        backgroundColor: 'var(--bg-card)',
        border: '1px solid var(--border-gold)',
        borderRadius: 4,
        flexShrink: 0,
        backgroundImage:
          'repeating-linear-gradient(45deg, transparent, transparent 8px, rgba(106, 85, 48, 0.15) 8px, rgba(106, 85, 48, 0.15) 9px), repeating-linear-gradient(-45deg, transparent, transparent 8px, rgba(106, 85, 48, 0.15) 8px, rgba(106, 85, 48, 0.15) 9px)',
      }}
    />
  )
}
