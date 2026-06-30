import { Skull } from 'lucide-react'

interface GraveyardStackProps {
  count: number
}

export default function GraveyardStack({ count }: GraveyardStackProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <Skull size={20} strokeWidth={1.5} style={{ color: 'var(--text-muted)' }} />
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
