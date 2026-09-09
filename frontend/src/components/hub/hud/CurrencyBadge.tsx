import type { LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

interface CurrencyBadgeProps {
  count: string | number
  label: string
  Icon: LucideIcon
  variant?: 'gold' | 'blue'
}

const variantStyles = {
  gold: {
    icon: 'bg-[radial-gradient(circle_at_35%_30%,var(--color-gold-light),var(--color-gold-dark))] text-bg-darkest',
    count: 'text-gold-light',
    label: 'text-text-muted',
  },
  blue: {
    icon: 'bg-[radial-gradient(circle_at_35%_30%,var(--color-blue-light),var(--color-blue))] text-blue-dark',
    count: 'text-blue-light',
    label: 'text-blue-dim',
  },
}

export default function CurrencyBadge({
  count,
  label,
  Icon,
  variant = 'gold',
}: CurrencyBadgeProps) {
  const styles = variantStyles[variant]

  return (
    <div className="flex items-center gap-2.5 rounded-3xl bg-linear-to-b from-bg-card to-bg-dark border border-border-subtle py-[7px] pl-[7px] pr-[15px] shadow-[0_4px_10px_rgba(0,0,0,.4)]">
      <div className={cn('size-7 rounded-full flex items-center justify-center', styles.icon)}>
        <Icon size={15} strokeWidth={2} />
      </div>
      <div className="leading-none">
        <div className={cn('font-heading font-bold text-[15px]', styles.count)}>
          {count}
        </div>
        <div className={cn('text-[8.5px] tracking-[1.5px] uppercase', styles.label)}>
          {label}
        </div>
      </div>
    </div>
  )
}
