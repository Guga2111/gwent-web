import { Snowflake, CloudFog, CloudRain, type LucideIcon } from 'lucide-react'

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
]

export default function WeatherZone({ weatherEffects, isTargeting, targetAbility, onSlotClick }: WeatherZoneProps) {
  const activeSet = new Set((weatherEffects ?? []).map((e) => e.toUpperCase()))
  const isClearWeather = targetAbility === 'CLEAR_WEATHER'

  return (
    <div className="flex flex-col gap-2 p-3 mx-2 my-1 rounded border border-border-subtle bg-[rgba(13,10,7,0.5)]" style={{ minHeight: '110px' }}>
      {WEATHER_SLOTS.map(({ key, label, Icon }) => {
        const active = activeSet.has(key)
        const isTarget = isTargeting && (isClearWeather || targetAbility === key)
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
