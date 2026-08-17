import client from './client'
import type { CatalogCardDto, Faction } from '@/types/deck'

export async function getCardsByFaction(faction: Faction, includeNeutral = true): Promise<CatalogCardDto[]> {
  const response = await client.get<CatalogCardDto[]>('/api/catalog', {
    params: { faction, includeNeutral },
  })
  return response.data
}