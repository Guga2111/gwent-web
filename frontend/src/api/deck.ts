import client from './client'
import type { DeckDto, SaveDeckRequest } from '@/types/deck'

export async function getUserDecks(): Promise<DeckDto[]> {
  const response = await client.get<DeckDto[]>('/api/decks')
  return response.data
}

export async function getDeck(id: string): Promise<DeckDto> {
  const response = await client.get<DeckDto>(`/api/decks/${id}`)
  return response.data
}

export async function createDeck(request: SaveDeckRequest): Promise<DeckDto> {
  const response = await client.post<DeckDto>('/api/decks', request)
  return response.data
}

export async function updateDeck(id: string, request: SaveDeckRequest): Promise<DeckDto> {
  const response = await client.put<DeckDto>(`/api/decks/${id}`, request)
  return response.data
}

export async function deleteDeck(id: string): Promise<void> {
  await client.delete(`/api/decks/${id}`)
}
