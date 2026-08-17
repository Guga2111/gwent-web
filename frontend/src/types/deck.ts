export type Faction = 'MONSTER' | 'NILFGAARD' | 'NORTHERN_REALMS' | 'SCOIATAEL' | 'SKELLIGE' | 'NEUTRAL'

export interface DeckCardEntryDto {
  cardId: string
  quantity: number
}

export interface DeckDto {
  id: string
  name: string
  faction: Faction
  leaderId: string
  cards: DeckCardEntryDto[]
  createdAt: string
}

export interface SaveDeckRequest {
  name: string
  faction: Faction
  leaderId: string
  cards: DeckCardEntryDto[]
}

export interface CatalogCardDto {
  id: string
  name: string
  faction: Faction
  cardType: 'UNIT' | 'HERO' | 'SPECIAL' | 'WEATHER' | 'LEADER'
  ability: string | null
  leaderAbility: string | null
  rowType: 'MELEE' | 'RANGED' | 'SIEGE' | 'AGILE' | null
  basePower: number | null
  deckCopies: number
}
