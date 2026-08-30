import type { Faction } from '@/types/deck'

export interface FactionConfig {
  label: string
  primaryVar: string
  secondaryVar: string
  accentColor: string
}

const FACTION_MAP: Record<string, FactionConfig> = {
  NORTHERN_REALMS: { label: 'Reinos do Norte', primaryVar: '--faction-northern-primary', secondaryVar: '--faction-northern-secondary', accentColor: '--faction-northern-primary' },
  NILFGAARD:       { label: 'Nilfgaard',        primaryVar: '--faction-nilfgaard-primary', secondaryVar: '--faction-nilfgaard-secondary', accentColor: '--faction-nilfgaard-primary' },
  MONSTER:         { label: 'Monstros',          primaryVar: '--faction-monsters-primary',  secondaryVar: '--faction-monsters-secondary',  accentColor: '--faction-monsters-primary' },
  SCOIATAEL:       { label: "Scoiata'el",        primaryVar: '--faction-scoiatael-primary', secondaryVar: '--faction-scoiatael-secondary', accentColor: '--faction-scoiatael-primary' },
  SKELLIGE:        { label: 'Skellige',           primaryVar: '--faction-skellige-primary',  secondaryVar: '--faction-skellige-secondary',  accentColor: '--faction-skellige-primary' },
  NEUTRAL:         { label: 'Neutro',             primaryVar: '--gold',                      secondaryVar: '--bg-medium',                   accentColor: '--gold' },
}

const NEUTRAL_CONFIG: FactionConfig = FACTION_MAP.NEUTRAL

export function getFactionConfig(faction: Faction | null): FactionConfig {
  if (!faction) return NEUTRAL_CONFIG
  return FACTION_MAP[faction] ?? NEUTRAL_CONFIG
}
