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
      className="nav-card relative flex flex-col items-center gap-[7px] w-[122px] px-2.5 py-[13px] pb-3 border-none cursor-pointer rounded-[9px]"
    >
      {/* Glow overlay */}
      <div
        className={`absolute inset-0 rounded-[9px] pointer-events-none transition-all duration-200 ${active ? 'nav-card__glow--active' : ''}`}
      />
      {/* Medallion icon */}
      <div
        className={`relative shrink-0 w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200 ${active ? 'nav-card__medallion--active' : 'nav-card__medallion'}`}
      >
        <Icon size={19} strokeWidth={1.9} />
      </div>
      {/* Label */}
      <span
        className={`font-heading relative font-semibold text-[11.5px] tracking-[.4px] leading-[1.15] min-h-[2lh] text-center flex items-center justify-center transition-colors duration-200 text-text-muted ${active ? 'nav-card__label--active' : ''}`}
      >
        {label}
      </span>
    </button>
  )
}
