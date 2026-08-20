import { Snowflake, CloudFog, CloudRain, type LucideIcon } from 'lucide-react'

interface WeatherZoneProps {
  weatherEffects: string[]
}

const WEATHER_SLOTS: { key: string; label: string; Icon: LucideIcon }[] = [
  { key: 'FROST', label: 'Geada', Icon: Snowflake },
  { key: 'FOG', label: 'Névoa', Icon: CloudFog },
  { key: 'RAIN', label: 'Chuva', Icon: CloudRain },
]

export default function WeatherZone({ weatherEffects }: WeatherZoneProps) {
  const activeSet = new Set((weatherEffects ?? []).map((e) => e.toUpperCase()))

  return (
    <div className="flex flex-col gap-1 p-2 mx-2 my-1 rounded border border-[var(--border-subtle)] bg-[rgba(13,10,7,0.5)]">
      {WEATHER_SLOTS.map(({ key, label, Icon }) => {
        const active = activeSet.has(key)
        return (
          <div key={key} className="flex items-center gap-1.5 text-[11px]">
            <Icon
              size={14}
              strokeWidth={1.5}
              className="shrink-0"
              style={{ color: active ? 'var(--gold)' : 'var(--text-muted)' }}
            />
            <span
              className={active ? 'text-[var(--text-primary)]' : 'text-[var(--text-muted)]'}
              style={{ fontFamily: 'var(--font-ui)' }}
            >
              {label}
            </span>
          </div>
        )
      })}
    </div>
  )
}
