interface GraveyardStackProps {
  count: number
}

export default function GraveyardStack({ count }: GraveyardStackProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <div style={{ fontSize: 20, color: 'var(--text-muted)' }}>☠</div>
      <div
        style={{
          fontSize: 11,
          fontFamily: 'var(--font-ui)',
          color: 'var(--text-muted)',
        }}
      >
        {count}
      </div>
    </div>
  )
}
