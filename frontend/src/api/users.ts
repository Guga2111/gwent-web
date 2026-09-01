import client from './client'
import type { UserMeDto } from '@/types/auth'

export async function getMe(): Promise<UserMeDto> {
  const response = await client.get<UserMeDto>('/api/users/me')
  return response.data
}

export async function markTutorialSeen(): Promise<void> {
  await client.post('/api/users/me/tutorial-seen')
}
