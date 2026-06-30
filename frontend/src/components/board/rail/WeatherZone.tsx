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
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: 4,
        padding: '8px 12px',
        margin: '4px 8px',
        borderRadius: 4,
        backgroundColor: 'rgba(13, 10, 7, 0.5)',
        border: '1px solid var(--border-subtle)',
      }}
    >
      {WEATHER_SLOTS.map(({ key, label, Icon }) => {
        const active = activeSet.has(key)
        return (
          <div
            key={key}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              fontSize: 11,
            }}
          >
            <Icon
              size={14}
              strokeWidth={1.5}
              style={{ color: active ? 'var(--gold)' : 'var(--text-muted)', flexShrink: 0 }}
            />
            <span
              style={{
                fontFamily: 'var(--font-ui)',
                color: active ? 'var(--text-primary)' : 'var(--text-muted)',
              }}
            >
              {label}
            </span>
          </div>
        )
      })}
    </div>
  )
}
