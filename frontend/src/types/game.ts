export type Faction = 'NORTHERN_REALMS' | 'NILFGAARD' | 'MONSTER' | 'SCOIATAEL' | 'SKELLIGE'
export type CardType = 'UNIT' | 'HERO' | 'WEATHER' | 'SPECIAL' | 'LEADER'
export type RowType = 'MELEE' | 'RANGED' | 'SIEGE'
export type Ability = 'NONE' | 'SPY' | 'TIGHT_BOND' | 'MORALE_BOOST' | 'MEDIC' | 'MUSTER' | 'SCORCH' | 'DUMMY' | 'COMMANDERS_HORN' | 'BERSERKER' | 'AGILE' | 'FROST' | 'FOG' | 'RAIN' | 'CLEAR_WEATHER'

export type LeaderAbility =
  | 'SIEGE_MASTER' | 'SON_OF_MEDELL' | 'KING_OF_TEMERIA' | 'LORD_COMMANDER' | 'STEEL_FORGED'
  | 'EMPEROR_OF_NILFGAARD' | 'INVADER_OF_THE_NORTH' | 'RELENTLESS' | 'WHITE_FLAME' | 'IMPERIAL_MAJESTY'
  | 'BRINGER_OF_DEATH' | 'COMMANDER_OF_THE_RED_RIDERS' | 'DESTROYER_OF_WORLDS' | 'KING_OF_THE_WILD_HUNT' | 'TREACHEROUS'
  | 'QUEEN_OF_DOL_BLATHANNA' | 'DAISY_OF_THE_VALLEY' | 'PUREBLOOD_ELF' | 'THE_BEATIFUL' | 'HOPE_OF_THE_AEN_SEIDHE'
  | 'KING_BRAN' | 'CLAN_AN_CRAITE'
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
  leaderAbility: LeaderAbility | null
}

export interface BoardRowDto {
  cards: CardDto[]
  hornActive: boolean
  weatherActive: boolean
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
  turnDeadlineUtc: number | null
  disconnectForfeit: boolean
}

export interface PresenceMessage {
  playerEmail: string
  connected: boolean
  forfeitDeadlineUtc: number | null
}

export interface CommandRequest {
  commandType: 'PLAY_CARD' | 'PASS' | 'MULLIGAN' | 'USE_LEADER' | 'RESOLVE_MEDIC' | 'CONFIRM_MULLIGAN' | 'RESOLVE_LEADER' | 'RESOLVE_SCOIATAEL'
  playerId?: string
  cardId?: string
  targetRow?: string
  cardIds?: string[]
  chosenPlayerId?: string
}
