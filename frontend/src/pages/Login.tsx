import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, register } from '@/api/auth'
import { useAuthStore } from '@/stores/authStore'

export default function Login() {
  const [isRegister, setIsRegister] = useState(false)
  const [email, setEmail] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const setToken = useAuthStore((s) => s.setToken)
  const navigate = useNavigate()

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (isRegister) {
        await register({ email, username, password })
      }
      const token = await login({ email, password })
      setToken(token)
      navigate('/hub')
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Authentication failed'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex h-full items-center justify-center">
      <div className="w-full max-w-sm rounded-lg border border-[var(--border-gold)] p-8 bg-[var(--bg-card)]">
        <h1
          className="mb-6 text-center text-2xl text-[var(--gold-light)]"
          style={{ fontFamily: 'var(--font-display)' }}
        >
          Gwent Online
        </h1>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="rounded border border-[var(--border-subtle)] px-3 py-2 outline-none focus:border-[var(--gold)] bg-[var(--bg-medium)] text-[var(--text-primary)]"
          />

          {isRegister && (
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="rounded border border-[var(--border-subtle)] px-3 py-2 outline-none focus:border-[var(--gold)] bg-[var(--bg-medium)] text-[var(--text-primary)]"
            />
          )}

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="rounded border border-[var(--border-subtle)] px-3 py-2 outline-none focus:border-[var(--gold)] bg-[var(--bg-medium)] text-[var(--text-primary)]"
          />

          {error && (
            <p className="text-sm text-[var(--red)]">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="rounded py-2 font-semibold transition-colors disabled:opacity-50 bg-[var(--gold-dark)] text-[var(--bg-darkest)]"
            style={{ fontFamily: 'var(--font-heading)' }}
          >
            {loading ? '...' : isRegister ? 'Register' : 'Login'}
          </button>
        </form>

        <button
          onClick={() => { setIsRegister(!isRegister); setError('') }}
          className="mt-4 w-full text-center text-sm underline text-[var(--text-secondary)]"
        >
          {isRegister ? 'Already have an account? Login' : 'Need an account? Register'}
        </button>
      </div>
    </div>
  )
}
