import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createGame, joinGame } from '@/api/game'
import { useAuthStore } from '@/stores/authStore'

export default function Lobby() {
  const [joinId, setJoinId] = useState('')
  const [createdId, setCreatedId] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  async function handleCreate() {
    setError('')
    setLoading(true)
    try {
      const { gameId } = await createGame()
      setCreatedId(gameId)
    } catch {
      setError('Failed to create game')
    } finally {
      setLoading(false)
    }
  }

  async function handleJoin() {
    if (!joinId.trim()) return
    setError('')
    setLoading(true)
    try {
      await joinGame(joinId.trim())
      navigate(`/game/${joinId.trim()}`)
    } catch {
      setError('Failed to join game')
    } finally {
      setLoading(false)
    }
  }

  function handleGoToGame() {
    if (createdId) navigate(`/game/${createdId}`)
  }

  return (
    <div className="flex h-full flex-col items-center justify-center gap-8">
      {/* Header */}
      <div className="text-center">
        <h1
          className="text-3xl gold-glow"
          style={{ fontFamily: 'var(--font-display)', color: 'var(--gold-light)' }}
        >
          Taverna
        </h1>
        <p className="mt-2 text-sm" style={{ color: 'var(--text-secondary)' }}>
          Welcome, {user?.username ?? user?.email}
        </p>
      </div>

      <div className="flex gap-6">
        {/* Create Game */}
        <div
          className="flex w-72 flex-col gap-4 rounded-lg border p-6"
          style={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border-gold)' }}
        >
          <h2 className="text-lg" style={{ fontFamily: 'var(--font-heading)', color: 'var(--gold)' }}>
            Create Game
          </h2>
          <button
            onClick={handleCreate}
            disabled={loading}
            className="rounded py-2 font-semibold transition-colors disabled:opacity-50"
            style={{
              backgroundColor: 'var(--gold-dark)',
              color: 'var(--bg-darkest)',
              fontFamily: 'var(--font-heading)',
            }}
          >
            {loading ? '...' : 'New Game'}
          </button>
          {createdId && (
            <div className="flex flex-col gap-2">
              <p className="text-xs" style={{ color: 'var(--text-secondary)' }}>
                Share this ID with your opponent:
              </p>
              <code
                className="select-all rounded px-2 py-1 text-center text-xs break-all"
                style={{ backgroundColor: 'var(--bg-medium)', color: 'var(--gold-light)' }}
              >
                {createdId}
              </code>
              <button
                onClick={handleGoToGame}
                className="rounded py-1 text-sm"
                style={{ backgroundColor: 'var(--bg-hover)', color: 'var(--gold)' }}
              >
                Enter Game
              </button>
            </div>
          )}
        </div>

        {/* Join Game */}
        <div
          className="flex w-72 flex-col gap-4 rounded-lg border p-6"
          style={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border-gold)' }}
        >
          <h2 className="text-lg" style={{ fontFamily: 'var(--font-heading)', color: 'var(--gold)' }}>
            Join Game
          </h2>
          <input
            type="text"
            placeholder="Paste Game ID"
            value={joinId}
            onChange={(e) => setJoinId(e.target.value)}
            className="rounded border px-3 py-2 text-sm outline-none focus:border-[var(--gold)]"
            style={{
              backgroundColor: 'var(--bg-medium)',
              borderColor: 'var(--border-subtle)',
              color: 'var(--text-primary)',
            }}
          />
          <button
            onClick={handleJoin}
            disabled={loading || !joinId.trim()}
            className="rounded py-2 font-semibold transition-colors disabled:opacity-50"
            style={{
              backgroundColor: 'var(--gold-dark)',
              color: 'var(--bg-darkest)',
              fontFamily: 'var(--font-heading)',
            }}
          >
            Join
          </button>
        </div>
      </div>

      {error && (
        <p className="text-sm" style={{ color: 'var(--red)' }}>{error}</p>
      )}

      <button
        onClick={() => { logout(); navigate('/login') }}
        className="text-sm underline"
        style={{ color: 'var(--text-muted)' }}
      >
        Logout
      </button>
    </div>
  )
}