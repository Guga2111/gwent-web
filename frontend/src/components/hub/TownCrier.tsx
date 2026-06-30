import { Volume2 } from 'lucide-react'

export default function TownCrier() {
  return (
    <div
      style={{
        position: 'relative',
        zIndex: 3,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 11,
        padding: '7px 24px',
        color: 'var(--gold)',
        flexShrink: 0,
      }}
    >
      <Volume2 size={15} strokeWidth={2} style={{ flexShrink: 0 }} />
      <span
        style={{
          fontFamily: 'var(--font-body)',
          fontStyle: 'italic',
          fontSize: 14,
          color: 'var(--text-secondary)',
        }}
      >
        Bem-vindo à Taverna do Continente. Encontre seus oponentes e forje seus baralhos.
      </span>
    </div>
  )
}
