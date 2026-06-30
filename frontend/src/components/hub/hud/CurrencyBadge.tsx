import type { LucideIcon } from 'lucide-react'

interface CurrencyBadgeProps {
  count: string | number
  label: string
  Icon: LucideIcon
  iconBg: string
  iconColor: string
  accentShadow: string
  countColor?: string
  labelColor?: string
}

export default function CurrencyBadge({
  count,
  label,
  Icon,
  iconBg,
  iconColor,
  accentShadow,
  countColor = 'var(--gold-light)',
  labelColor = 'var(--text-muted)',
}: CurrencyBadgeProps) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 9,
        padding: '7px 15px 7px 7px',
        borderRadius: 24,
        background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
        boxShadow: `inset 0 0 0 1px ${accentShadow}, 0 4px 10px rgba(0,0,0,.4)`,
      }}
    >
      <div
        style={{
          width: 28,
          height: 28,
          borderRadius: '50%',
          background: iconBg,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: iconColor,
        }}
      >
        <Icon size={15} strokeWidth={2} />
      </div>
      <div style={{ lineHeight: 1 }}>
        <div
          style={{
            fontFamily: 'var(--font-heading)',
            fontWeight: 700,
            color: countColor,
            fontSize: 15,
          }}
        >
          {count}
        </div>
        <div
          style={{
            fontSize: '8.5px',
            letterSpacing: '1.5px',
            textTransform: 'uppercase',
            color: labelColor,
          }}
        >
          {label}
        </div>
      </div>
    </div>
  )
}
