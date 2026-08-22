import { Sword, BowArrow } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import type { RowType } from '@/types/game'

interface RowIconProps {
  rowType: RowType
  size?: 'sm' | 'lg'
}

function SiegeIcon({ size, strokeWidth }: { size: number; strokeWidth: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {/* Base platform */}
      <line x1="3" y1="20" x2="21" y2="20" />
      {/* Left leg */}
      <line x1="7" y1="20" x2="7" y2="14" />
      {/* Right leg */}
      <line x1="17" y1="20" x2="17" y2="14" />
      {/* Cross beam */}
      <line x1="5" y1="14" x2="19" y2="14" />
      {/* Pivot */}
      <line x1="12" y1="14" x2="12" y2="12" />
      {/* Throwing arm */}
      <line x1="6" y1="6" x2="16" y2="12" />
      {/* Counterweight */}
      <circle cx="16" cy="12" r="1.5" />
      {/* Projectile sling */}
      <path d="M6 6 L4 4" />
    </svg>
  )
}

const ROW_ICONS: Record<RowType, LucideIcon | typeof SiegeIcon> = {
  MELEE: Sword,
  RANGED: BowArrow,
  SIEGE: SiegeIcon,
}

export default function RowIcon({ rowType, size = 'sm' }: RowIconProps) {
  const Icon = ROW_ICONS[rowType]
  const iconSize = size === 'sm' ? 14 : 28
  return (
    <div className={size === 'lg' ? 'card-detail-row-icon' : 'card-row-icon'}>
      <Icon size={iconSize} strokeWidth={2} />
    </div>
  )
}
