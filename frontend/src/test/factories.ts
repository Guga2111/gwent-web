import type { CardDto, BoardRowDto, PlayerStateDto, OpponentStateDto, GameStateDto } from '@/types/game'

export function makeCard(overrides: Partial<CardDto> = {}): CardDto {
  return {
    id: 'card-1',
    name: 'Test Card',
    basePower: 5,
    currentPower: 5,
    cardType: 'UNIT',
    rowType: 'MELEE',
    ability: 'NONE',
    faction: 'NORTHERN_REALMS',
    leaderAbility: null,
    ...overrides,
  }
}

export function makeBoardRow(overrides: Partial<BoardRowDto> = {}): BoardRowDto {
  return {
    cards: [],
    hornActive: false,
    weatherActive: false,
    ...overrides,
  }
}

export function makeLeaderCard(overrides: Partial<CardDto> = {}): CardDto {
  return makeCard({
    id: 'leader-1',
    name: 'Leader',
    basePower: null,
    currentPower: null,
    cardType: 'LEADER',
    rowType: null,
    ability: null,
    leaderAbility: 'KING_OF_TEMERIA',
    ...overrides,
  })
}

export function makePlayerState(overrides: Partial<PlayerStateDto> = {}): PlayerStateDto {
  return {
    playerId: 'player@test.com',
    lives: 2,
    score: 0,
    passed: false,
    leaderUsed: false,
    leader: makeLeaderCard(),
    mulligansRemaining: 2,
    mulliganConfirmed: false,
    hand: [],
    deckSize: 20,
    meleeRow: makeBoardRow(),
    rangedRow: makeBoardRow(),
    siegeRow: makeBoardRow(),
    graveyard: [],
    ...overrides,
  }
}

export function makeOpponentState(overrides: Partial<OpponentStateDto> = {}): OpponentStateDto {
  return {
    playerId: 'opponent@test.com',
    lives: 2,
    score: 0,
    passed: false,
    leaderUsed: false,
    leader: makeLeaderCard({ id: 'leader-2' }),
    handSize: 10,
    deckSize: 20,
    meleeRow: makeBoardRow(),
    rangedRow: makeBoardRow(),
    siegeRow: makeBoardRow(),
    graveyard: [],
    ...overrides,
  }
}

export function makeGameState(overrides: Partial<GameStateDto> = {}): GameStateDto {
  return {
    gameId: 'game-1',
    phase: 'PLAY',
    currentTurn: 'PLAYER_1',
    myTurn: 'PLAYER_1',
    pendingAbility: null,
    currentRound: 1,
    weatherCards: [],
    me: makePlayerState(),
    opponent: makeOpponentState(),
    revealedCards: null,
    deckCards: null,
    winner: null,
    endReason: null,
    turnDeadlineUtc: null,
    abilityDeadlineUtc: null,
    disconnectForfeit: false,
    ...overrides,
  }
}
