import type { Faction } from '@/types/deck'
import {
  NorthernRealmsEmblem,
  NilfgaardEmblem,
  MonstersEmblem,
  ScoiataelEmblem,
  SkelligeEmblem,
} from '@/icons/faction-emblems'

export interface FactionConfig {
  label: string
  primaryVar: string
  secondaryVar: string
  accentColor: string
  tokens: { primary: string; secondary: string; accent: string }
  Emblem: (() => React.JSX.Element) | null
}

const FACTION_MAP: Record<string, FactionConfig> = {
  NORTHERN_REALMS: { label: 'Reinos do Norte', primaryVar: '--faction-northern-primary', secondaryVar: '--faction-northern-secondary', accentColor: 'faction-northern', tokens: { primary: '#c0cce0', secondary: '#3568b8', accent: '#3568b8' }, Emblem: NorthernRealmsEmblem },
  NILFGAARD:       { label: 'Nilfgaard',        primaryVar: '--faction-nilfgaard-primary', secondaryVar: '--faction-nilfgaard-secondary', accentColor: 'faction-nilfgaard', tokens: { primary: '#c8a84e', secondary: '#2a2a2e', accent: '#c8a84e' }, Emblem: NilfgaardEmblem },
  MONSTER:         { label: 'Monstros',          primaryVar: '--faction-monsters-primary',  secondaryVar: '--faction-monsters-secondary',  accentColor: 'faction-monsters',  tokens: { primary: '#c43030', secondary: '#1a0808', accent: '#c43030' },  Emblem: MonstersEmblem },
  SCOIATAEL:       { label: "Scoiata'el",        primaryVar: '--faction-scoiatael-primary', secondaryVar: '--faction-scoiatael-secondary', accentColor: 'faction-scoiatael', tokens: { primary: '#8a7a3a', secondary: '#2a5a2a', accent: '#8a7a3a' }, Emblem: ScoiataelEmblem },
  SKELLIGE:        { label: 'Skellige',           primaryVar: '--faction-skellige-primary',  secondaryVar: '--faction-skellige-secondary',  accentColor: 'faction-skellige',  tokens: { primary: '#8a8a9a', secondary: '#4a2a6a', accent: '#8a8a9a' },  Emblem: SkelligeEmblem },
  NEUTRAL:         { label: 'Neutro',             primaryVar: '--gold',                      secondaryVar: '--bg-medium',                   accentColor: 'gold',              tokens: { primary: '#caa057', secondary: '#2a2118', accent: '#caa057' },   Emblem: null },
}

const NEUTRAL_CONFIG: FactionConfig = FACTION_MAP.NEUTRAL

export function getFactionConfig(faction: Faction | null): FactionConfig {
  if (!faction) return NEUTRAL_CONFIG
  return FACTION_MAP[faction] ?? NEUTRAL_CONFIG
}
