import { Swords } from 'lucide-react'

export default function CentralDivider() {
  return (
    <div className="relative h-px bg-[var(--border-gold)] shrink-0">
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-[var(--bg-dark)] border border-[var(--border-gold)] flex items-center justify-center text-[var(--gold)] z-10">
        <Swords size={16} strokeWidth={1.5} />
      </div>
    </div>
  )
}
