import client from './client'

// Returns the gameId if a match was found immediately (HTTP 200 with body),
// or null if the player was placed in the queue (HTTP 202 with no body).
export async function joinMatchmakingQueue(deckId: string): Promise<string | null> {
  const res = await client.post<{ gameId: string } | null>('/api/matchmaking/join', { deckId })
  return res.data?.gameId ?? null
}

export async function leaveMatchmakingQueue(): Promise<void> {
  await client.post('/api/matchmaking/leave')
}
