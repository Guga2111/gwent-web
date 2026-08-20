export type Faction = 'NORTHERN_REALMS' | 'NILFGAARD' | 'MONSTERS' | 'SCOIATAEL' | 'SKELLIGE'
export type CardType = 'UNIT' | 'HERO' | 'WEATHER' | 'SPECIAL' | 'LEADER'
export type RowType = 'MELEE' | 'RANGED' | 'SIEGE'
export type Ability = 'NONE' | 'SPY' | 'BOND' | 'MORALE' | 'MEDIC' | 'MUSTER' | 'SCORCH' | 'DECOY' | 'HORN' | 'BERSERKER' | 'AGILE'
export type GamePhase = 'COIN_FLIP' | 'REDRAW' | 'PLAY' | 'ROUND_END' | 'GAME_OVER'
export type Turn = 'PLAYER_1' | 'PLAYER_2'

export interface CardDto {
  id: string
  name: string
  basePower: number | null
  currentPower: number | null
  cardType: CardType
  rowType: RowType | null
  ability: Ability | null
  faction: Faction
}

export interface BoardRowDto {
  cards: CardDto[]
  hornActive: boolean
  weatherActive: boolean
  leaderBonusPower: number
}

export interface PlayerStateDto {
  playerId: string
  lives: number
  score: number
  passed: boolean
  leaderUsed: boolean
  leader: CardDto
  mulligansRemaining: number
  mulliganConfirmed: boolean
  hand: CardDto[]
  deckSize: number
  meleeRow: BoardRowDto
  rangedRow: BoardRowDto
  siegeRow: BoardRowDto
  graveyard: CardDto[]
}

export interface OpponentStateDto {
  playerId: string
  lives: number
  score: number
  passed: boolean
  leaderUsed: boolean
  leader: CardDto
  handSize: number
  deckSize: number
  meleeRow: BoardRowDto
  rangedRow: BoardRowDto
  siegeRow: BoardRowDto
  graveyard: CardDto[]
}

export interface GameStateDto {
  gameId: string
  phase: GamePhase
  currentTurn: Turn
  myTurn: Turn
  pendingAbility: string | null
  currentRound: number
  weatherCards: CardDto[]
  me: PlayerStateDto
  opponent: OpponentStateDto
  revealedCards: CardDto[] | null
  deckCards: CardDto[] | null
  winner: string | null
  endReason: string | null
}

export interface CommandRequest {
  commandType: 'PLAY_CARD' | 'PASS' | 'MULLIGAN' | 'USE_LEADER' | 'RESOLVE_MEDIC' | 'CONFIRM_MULLIGAN' | 'RESOLVE_LEADER'
  playerId?: string
  cardId?: string
  targetRow?: string
  cardIds?: string[]
}
