import {
  Eye,
  Link,
  ChevronUp,
  HeartPulse,
  Users,
  Flame,
  Target,
  Megaphone,
  Axe,
  RefreshCcw,
  Snowflake,
  CloudFog,
  CloudRain,
  Sun,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import type { Ability } from '@/types/game'

interface AbilityIconProps {
  ability: Ability
  size?: 'sm' | 'lg'
}

const ABILITY_ICONS: Record<string, LucideIcon> = {
  SPY: Eye,
  TIGHT_BOND: Link,
  MORALE_BOOST: ChevronUp,
  MEDIC: HeartPulse,
  MUSTER: Users,
  SCORCH: Flame,
  DUMMY: Target,
  COMMANDERS_HORN: Megaphone,
  BERSERKER: Axe,
  AGILE: RefreshCcw,
  FROST: Snowflake,
  FOG: CloudFog,
  RAIN: CloudRain,
  CLEAR_WEATHER: Sun,
}

export default function AbilityIcon({ ability, size = 'sm' }: AbilityIconProps) {
  const Icon = ABILITY_ICONS[ability]
  if (!Icon) return null

  const iconSize = size === 'sm' ? 10 : 18
  const strokeWidth = size === 'sm' ? 1.5 : 2

  return (
    <div className={size === 'lg' ? 'card-detail-ability' : 'card-ability-medallion'}>
      <Icon size={iconSize} strokeWidth={strokeWidth} />
    </div>
  )
}
