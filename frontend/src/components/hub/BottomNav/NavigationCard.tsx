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
      className="relative flex flex-col items-center gap-[7px] w-[122px] px-2.5 py-[13px] pb-3 border-none cursor-pointer rounded-[9px]"
      style={{
        background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
        boxShadow: 'inset 0 0 0 1px rgba(20,12,5,.9), inset 0 0 0 2px rgba(240,205,120,.14), 0 9px 20px rgba(0,0,0,.45)',
      }}
    >
      {/* Glow overlay */}
      <div
        className="absolute inset-0 rounded-[9px] pointer-events-none transition-all duration-200"
        style={{
          background: active ? 'linear-gradient(180deg, rgba(240,210,122,.2), rgba(240,210,122,.04))' : 'transparent',
          boxShadow: active ? 'inset 0 0 0 2px rgba(240,210,122,.6), 0 0 22px rgba(240,200,110,.3)' : 'none',
        }}
      />
      {/* Medallion icon */}
      <div
        className="relative shrink-0 w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200"
        style={{
          background: active ? 'radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))' : 'rgba(0,0,0,.32)',
          boxShadow: active ? '0 3px 8px rgba(0,0,0,.45)' : 'inset 0 0 0 1px rgba(240,205,120,.18)',
          color: active ? 'var(--bg-darkest)' : 'var(--text-muted)',
        }}
      >
        <Icon size={19} strokeWidth={1.9} />
      </div>
      {/* Label */}
      <span
        className="relative font-semibold text-[11.5px] tracking-[.4px] leading-[1.15] min-h-[2lh] text-center flex items-center justify-center transition-colors duration-200"
        style={{
          fontFamily: 'var(--font-heading)',
          color: active ? 'var(--gold-light)' : 'var(--text-muted)',
          textShadow: active ? '0 0 8px rgba(202,160,87,0.6)' : 'none',
        }}
      >
        {label}
      </span>
    </button>
  )
}
