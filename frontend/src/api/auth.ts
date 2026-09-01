import client from './client'
import type { LoginRequest, RegisterRequest, RegisterResponse } from '@/types/auth'

export async function login(data: LoginRequest): Promise<string> {
  const response = await client.post('/authenticate', data)
  const token = response.headers['authorization']?.replace('Bearer ', '')
  if (!token) throw new Error('No token received')
  return token
}

export async function register(data: RegisterRequest): Promise<RegisterResponse> {
  const response = await client.post<RegisterResponse>('/api/auth/register', data)
  return response.data
}