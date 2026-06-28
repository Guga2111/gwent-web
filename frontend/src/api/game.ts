import client from './client'

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