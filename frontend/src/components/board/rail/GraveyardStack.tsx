import { Skull } from 'lucide-react'

interface GraveyardStackProps {
  count: number
}

export default function GraveyardStack({ count }: GraveyardStackProps) {
  return (
    <div className="flex flex-col items-center gap-0.5">
      <Skull size={20} strokeWidth={1.5} className="text-[var(--text-muted)]" />
      <div
        className="text-[11px] text-[var(--text-muted)]"
        style={{ fontFamily: 'var(--font-ui)' }}
      >
        {count}
      </div>
    </div>
  )
}
