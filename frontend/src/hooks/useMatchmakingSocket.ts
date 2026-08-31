import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '@/stores/authStore'
import { useMatchmakingStore } from '@/stores/matchmakingStore'

export function useMatchmakingSocket(active: boolean) {
  const clientRef = useRef<Client | null>(null)
  const token = useAuthStore((s) => s.token)
  const user = useAuthStore((s) => s.user)

  useEffect(() => {
    if (!active || !user) return

    let isActive = true

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 2000,
      onConnect: () => {
        if (!isActive) return
        stompClient.subscribe(`/topic/matchmaking/${user.email}`, (message) => {
          if (!isActive) return
          const payload = JSON.parse(message.body)
          if (payload.type === 'MATCH_FOUND') {
            useMatchmakingStore.getState().setFound(payload.gameId)
          } else if (payload.type === 'TIMEOUT') {
            useMatchmakingStore.getState().setError('Tempo esgotado. Nenhum oponente encontrado.')
          }
        })
      },
      onStompError: (frame) => {
        if (!isActive) return
        console.error('STOMP matchmaking error:', frame.headers['message'])
        useMatchmakingStore.getState().setError('Erro na conexão. Tente novamente.')
      },
    })

    clientRef.current = stompClient
    stompClient.activate()

    return () => {
      isActive = false
      stompClient.deactivate()
      clientRef.current = null
    }
  }, [active, token, user])
}
