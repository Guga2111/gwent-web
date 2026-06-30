import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import type { GameStateDto, CommandRequest } from '@/types/game'

export function useWebSocket(gameId: string | null) {
  const clientRef = useRef<Client | null>(null)
  const setGameState = useGameStore((s) => s.setGameState)
  const setConnected = useGameStore((s) => s.setConnected)
  const setError = useGameStore((s) => s.setError)
  const token = useAuthStore((s) => s.token)

  useEffect(() => {
    if (!gameId) return

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        stompClient.subscribe(`/topic/games/${gameId}`, (message) => {
          const state: GameStateDto = JSON.parse(message.body)
          setGameState(state)
        })
        // TODO: adjust topic to match backend error handling config
        stompClient.subscribe(`/topic/games/${gameId}/errors`, (message) => {
          setError(message.body)
        })
      },
      onDisconnect: () => {
        setConnected(false)
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'])
        setConnected(false)
      },
    })

    clientRef.current = stompClient
    stompClient.activate()

    return () => {
      stompClient.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [gameId, token, setGameState, setConnected, setError])

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
