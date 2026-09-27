import { Snowflake, CloudFog, CloudRain, CloudLightning, type LucideIcon } from 'lucide-react'

interface WeatherZoneProps {
  weatherEffects: string[]
  isTargeting?: boolean
  targetAbility?: string | null
  onSlotClick?: () => void
}

const WEATHER_SLOTS: { key: string; label: string; Icon: LucideIcon }[] = [
  { key: 'FROST', label: 'Geada', Icon: Snowflake },
  { key: 'FOG', label: 'Névoa', Icon: CloudFog },
  { key: 'RAIN', label: 'Chuva', Icon: CloudRain },
  { key: 'SKELLIGE_STORM', label: 'Tempestade', Icon: CloudLightning },
]

function isSlotTargeted(slotKey: string, targetAbility: string | null | undefined): boolean {
  if (!targetAbility) return false
  if (targetAbility === 'CLEAR_WEATHER') return true
  if (targetAbility === slotKey) return true
  if (targetAbility === 'SKELLIGE_STORM' && (slotKey === 'FOG' || slotKey === 'RAIN' || slotKey === 'SKELLIGE_STORM')) return true
  return false
}

export default function WeatherZone({ weatherEffects, isTargeting, targetAbility, onSlotClick }: WeatherZoneProps) {
  const activeSet = new Set((weatherEffects ?? []).map((e) => e.toUpperCase()))

  return (
    <div className="flex flex-col gap-2 p-3 mx-2 my-1 rounded border border-border-subtle bg-[rgba(13,10,7,0.5)]" style={{ minHeight: '110px' }}>
      {WEATHER_SLOTS.map(({ key, label, Icon }) => {
        const active = activeSet.has(key)
        const isTarget = isTargeting && isSlotTargeted(key, targetAbility)
        return (
          <div
            key={key}
            className={`flex items-center gap-1.5 text-[13px] rounded px-1 py-0.5${isTarget ? ' weather-slot--target' : ''}`}
            onClick={isTarget ? onSlotClick : undefined}
          >
            <Icon
              size={20}
              strokeWidth={1.5}
              className="shrink-0"
              style={{ color: active ? 'var(--gold)' : 'var(--text-muted)' }}
            />
            <span
              className={`${active ? 'text-text-primary' : 'text-text-muted'} font-ui`}
            >
              {label}
            </span>
          </div>
        )
      })}
    </div>
  )
}
