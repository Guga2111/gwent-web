import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import type { GameStateDto, CommandRequest } from '@/types/game'

export function useWebSocket(gameId: string | null) {
  const clientRef = useRef<Client | null>(null)
  const setGameState = useGameStore((s) => s.setGameState)
  const setConnected = useGameStore((s) => s.setConnected)
  const token = useAuthStore((s) => s.token)

  useEffect(() => {
    if (!gameId) return

    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${wsProtocol}//${window.location.host}/ws`

    const stompClient = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        stompClient.subscribe(`/topic/games/${gameId}`, (message) => {
          const state: GameStateDto = JSON.parse(message.body)
          setGameState(state)
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
  }, [gameId, token, setGameState, setConnected])

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
