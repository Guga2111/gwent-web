export type Faction = 'NORTHERN_REALMS' | 'NILFGAARD' | 'MONSTERS' | 'SCOIATAEL' | 'SKELLIGE'
export type CardType = 'UNIT' | 'HERO' | 'WEATHER' | 'SPECIAL' | 'LEADER'
export type RowType = 'MELEE' | 'RANGED' | 'SIEGE'
export type Ability = 'NONE' | 'SPY' | 'BOND' | 'MORALE' | 'MEDIC' | 'MUSTER' | 'SCORCH' | 'DECOY' | 'HORN' | 'BERSERKER' | 'AGILE'
export type GamePhase = 'COIN_FLIP' | 'REDRAW' | 'PLAY' | 'ROUND_END' | 'GAME_OVER'
export type Turn = 'PLAYER_1' | 'PLAYER_2'

export interface PlayerStateDto {
  playerId: string
  lives: number
  score: number
  passed: boolean
  leaderUsed: boolean
  mulligansRemaining: number
  mulliganConfirmed: boolean
  handCardIds: string[]
  meleeRowCardIds: string[]
  rangedRowCardIds: string[]
  siegeRowCardIds: string[]
  graveyardCardIds: string[]
}

export interface GameStateDto {
  gameId: string
  phase: GamePhase
  currentTurn: Turn
  pendingAbility: string | null
  currentRound: number
  player1: PlayerStateDto
  player2: PlayerStateDto
}

export interface CommandRequest {
  commandType: 'PLAY_CARD' | 'PASS' | 'MULLIGAN' | 'USE_LEADER' | 'RESOLVE_MEDIC' | 'CONFIRM_MULLIGAN'
  playerId?: string
  cardId?: string
  targetRow?: string
}
