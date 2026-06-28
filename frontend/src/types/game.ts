export type Faction = 'NORTHERN_REALMS' | 'NILFGAARD' | 'MONSTERS' | 'SCOIATAEL' | 'SKELLIGE'
export type CardType = 'UNIT' | 'HERO' | 'WEATHER' | 'SPECIAL'
export type RowType = 'CLOSE' | 'RANGED' | 'SIEGE'
export type Ability = 'NONE' | 'SPY' | 'BOND' | 'MORALE' | 'MEDIC' | 'MUSTER' | 'SCORCH' | 'DECOY' | 'HORN' | 'BERSERKER' | 'AGILE'
export type GamePhase = 'COIN_FLIP' | 'REDRAW' | 'PLAY' | 'ROUND_END' | 'GAME_OVER'
export type Turn = 'PLAYER_1' | 'PLAYER_2'

export interface Card {
  id: string
  name: string
  faction: Faction
  type: CardType
  row: RowType
  strength: number
  ability: Ability
  heroic: boolean
}

export interface BoardRowState {
  cards: Card[]
  hornActive: boolean
  score: number
}

export interface PlayerStateDto {
  playerId: string
  hand: Card[]
  close: BoardRowState
  ranged: BoardRowState
  siege: BoardRowState
  totalScore: number
  roundsWon: number
  passed: boolean
  leader: Card | null
  leaderUsed: boolean
  graveyardSize: number
  deckSize: number
}

export interface GameStateDto {
  gameId: string
  phase: GamePhase
  currentTurn: Turn
  player1: PlayerStateDto
  player2: PlayerStateDto
  weatherEffects: string[]
  round: number
}

export interface CommandRequest {
  commandType: 'PLAY_CARD' | 'PASS' | 'MULLIGAN' | 'USE_LEADER' | 'RESOLVE_MEDIC'
  playerId?: string
  cardId?: string
  row?: RowType
  targetCardId?: string
}