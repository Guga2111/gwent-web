import { create } from 'zustand'
import type { AuthUser } from '@/types/auth'

function parseJwt(token: string): AuthUser | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      email: payload.sub,
      username: payload.username,
      userId: payload.userId,
    }
  } catch {
    return null
  }
}

interface AuthState {
  token: string | null
  user: AuthUser | null
  setToken: (token: string) => void
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>((set, get) => {
  const storedToken = localStorage.getItem('jwt')
  const initialUser = storedToken ? parseJwt(storedToken) : null

  return {
    token: storedToken,
    user: initialUser,

    setToken: (token: string) => {
      localStorage.setItem('jwt', token)
      set({ token, user: parseJwt(token) })
    },

    logout: () => {
      localStorage.removeItem('jwt')
      set({ token: null, user: null })
    },

    isAuthenticated: () => get().token !== null,
  }
})
