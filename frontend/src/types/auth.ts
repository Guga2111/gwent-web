export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  username: string
  password: string
}

export interface RegisterResponse {
  id: string
  email: string
  username: string
}

export interface AuthUser {
  email: string
  username: string
  userId: string
}

export interface UserMeDto {
  email: string
  username: string
  hasSeenTutorial: boolean
}