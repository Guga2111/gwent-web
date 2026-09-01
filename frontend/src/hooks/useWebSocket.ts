import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import { getGameState } from '@/api/game'
import type { GameStateDto, CommandRequest, PresenceMessage } from '@/types/game'

export function useWebSocket(gameId: string | null) {
  const clientRef = useRef<Client | null>(null)
  const setGameState = useGameStore((s) => s.setGameState)
  const setConnected = useGameStore((s) => s.setConnected)
  const setError = useGameStore((s) => s.setError)
  const setOpponentPresence = useGameStore((s) => s.setOpponentPresence)
  const token = useAuthStore((s) => s.token)
  const user = useAuthStore((s) => s.user)

  useEffect(() => {
    if (!gameId || !user) return

    let active = true

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        if (!active) return
        setConnected(true)
        getGameState(gameId).then(setGameState).catch((err) => {
          console.error("Failed to fetch the game", err);
          setError("Falha ao carregar estado do jogo. Recarregue a página.");
        })
        stompClient.subscribe(`/topic/games/${gameId}/${user.email}`, (message) => {
          if (!active) return
          const state: GameStateDto = JSON.parse(message.body)
          setGameState(state)
        })
        stompClient.subscribe(`/topic/games/${gameId}/${user.email}/errors`, (message) => {
          if (!active) return
          setError(message.body)
        })
        stompClient.subscribe(`/topic/games/${gameId}/presence`, (message) => {
          if (!active) return
          const presence: PresenceMessage = JSON.parse(message.body)
          setOpponentPresence(presence.connected, presence.forfeitDeadlineUtc)
        })
      },
      onDisconnect: () => {
        if (!active) return
        setConnected(false)
      },
      onStompError: (frame) => {
        if (!active) return
        console.error('STOMP error:', frame.headers['message'])
        setConnected(false)
      },
    })

    clientRef.current = stompClient
    stompClient.activate()

    return () => {
      active = false
      stompClient.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [gameId, token, user, setGameState, setConnected, setError, setOpponentPresence])

  const sendCommand = useCallback(
    (command: CommandRequest) => {
      if (!clientRef.current?.connected || !gameId) return
      clientRef.current.publish({
        destination: `/app/games/${gameId}/command`,
        body: JSON.stringify(command),
      })
    },
    [gameId],
  )

  return { sendCommand }
}
