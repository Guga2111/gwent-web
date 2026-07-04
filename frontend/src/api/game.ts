import client from './client'
import type { GameStateDto } from '@/types/game'

interface CreateGameResponse {
  gameId: string
}

export async function createGame(): Promise<CreateGameResponse> {
  const response = await client.post<CreateGameResponse>('/api/games')
  return response.data
}

export async function joinGame(gameId: string): Promise<void> {
  await client.post(`/api/games/${gameId}/join`)
}

export async function getGameState(gameId: string): Promise<GameStateDto> {
  const response = await client.get<GameStateDto>(`/api/games/${gameId}`)
  return response.data
}

export async function surrender(gameId: string): Promise<void> {
  await client.post(`/api/games/${gameId}/surrender`)
}

export async function getActiveGame(): Promise<string | null> {
  const response = await client.get<{ gameId: string }>('/api/games/active')
  if (response.status === 204) return null
  return response.data.gameId
}