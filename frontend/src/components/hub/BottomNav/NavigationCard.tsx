import type { LucideIcon } from 'lucide-react'

interface NavigationCardProps {
  id: string
  label: string
  icon: LucideIcon
  active: boolean
  onClick: () => void
}

export default function NavigationCard({ label, icon: Icon, active, onClick }: NavigationCardProps) {
  return (
    <button
      onClick={onClick}
      style={{
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 7,
        width: 122,
        padding: '13px 10px 12px',
        border: 'none',
        cursor: 'pointer',
        borderRadius: 9,
        background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
        boxShadow:
          'inset 0 0 0 1px rgba(20,12,5,.9), inset 0 0 0 2px rgba(240,205,120,.14), 0 9px 20px rgba(0,0,0,.45)',
      }}
    >
      {/* Glow overlay */}
      <div
        style={{
          position: 'absolute',
          inset: 0,
          borderRadius: 9,
          background: active
            ? 'linear-gradient(180deg, rgba(240,210,122,.2), rgba(240,210,122,.04))'
            : 'transparent',
          boxShadow: active
            ? 'inset 0 0 0 2px rgba(240,210,122,.6), 0 0 22px rgba(240,200,110,.3)'
            : 'none',
          pointerEvents: 'none',
          transition: 'all .2s',
        }}
      />
      {/* Medallion icon */}
      <div
        style={{
          position: 'relative',
          flexShrink: 0,
          width: 40,
          height: 40,
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: active
            ? 'radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))'
            : 'rgba(0,0,0,.32)',
          boxShadow: active
            ? '0 3px 8px rgba(0,0,0,.45)'
            : 'inset 0 0 0 1px rgba(240,205,120,.18)',
          color: active ? 'var(--bg-darkest)' : 'var(--text-muted)',
          transition: 'all .2s',
        }}
      >
        <Icon size={19} strokeWidth={1.9} />
      </div>
      {/* Label */}
      <span
        style={{
          position: 'relative',
          fontFamily: 'var(--font-heading)',
          fontWeight: 600,
          fontSize: '11.5px',
          letterSpacing: '.4px',
          lineHeight: '1.15',
          minHeight: '2lh',
          textAlign: 'center',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: active ? 'var(--gold-light)' : 'var(--text-muted)',
          textShadow: active ? '0 0 8px rgba(202,160,87,0.6)' : 'none',
          transition: 'color .2s',
        }}
      >
        {label}
      </span>
    </button>
  )
}
