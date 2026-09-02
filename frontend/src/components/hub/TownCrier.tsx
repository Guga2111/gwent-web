import { Volume2 } from 'lucide-react'

export default function TownCrier() {
  return (
    <div className="relative z-30 flex items-center justify-center gap-[11px] px-6 py-[7px] text-gold shrink-0">
      <Volume2 size={15} strokeWidth={2} className="shrink-0" />
      <span
        className="text-sm text-text-secondary font-body"
      >
        Bem-vindo à Taverna do Continente. Encontre seus oponentes e forje seus baralhos.
      </span>
    </div>
  )
}
