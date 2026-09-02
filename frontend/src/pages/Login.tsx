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
    <div className="login-page">
      {/* Left panel — login form */}
      <div className="login-panel">
        <img src="/gwent-logo.svg" alt="Gwent Online" className="login-brand-logo" />

        <div className="login-card">
          <h1 className="login-card__title">
            {isRegister ? 'Registre Seu Brasão' : 'Entrar no Gwent'}
          </h1>
          <p className="login-card__subtitle">
            {isRegister
              ? 'Crie sua conta para entrar no jogo'
              : 'Entre com suas credenciais para jogar'}
          </p>

          <form onSubmit={handleSubmit} className="login-card__form">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="email" className="font-heading text-[11px] font-semibold uppercase tracking-[1.5px] text-text-secondary">Email</label>
              <input
                id="email"
                type="email"
                placeholder="seu@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input"
              />
            </div>

            {isRegister && (
              <div className="flex flex-col gap-1.5">
                <label htmlFor="username" className="font-heading text-[11px] font-semibold uppercase tracking-[1.5px] text-text-secondary">Nome de Usuário</label>
                <input
                  id="username"
                  type="text"
                  placeholder="Escolha um nome"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  className="form-input"
                />
              </div>
            )}

            <div className="flex flex-col gap-1.5">
              <label htmlFor="password" className="font-heading text-[11px] font-semibold uppercase tracking-[1.5px] text-text-secondary">Senha</label>
              <input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="form-input"
              />
            </div>

            {error && <p className="font-ui text-[13px] text-red">{error}</p>}

            <button type="submit" disabled={loading} className="btn-gold w-full py-3 rounded-lg border-none cursor-pointer font-bold text-sm text-bg-darkest tracking-widest uppercase disabled:opacity-50 disabled:cursor-not-allowed">
              {loading ? '...' : isRegister ? 'Registrar' : 'Entrar'}
            </button>
          </form>

          <div className="form-divider">
            <span className="form-divider__text">ou</span>
          </div>

          <div className="login-card__footer">
            <button
              type="button"
              onClick={() => { setIsRegister(!isRegister); setError('') }}
              className="form-link"
            >
              {isRegister
                ? 'Já tem uma conta? '
                : 'Não tem uma conta? '}
              <span className="form-link__highlight">
                {isRegister ? 'Entrar' : 'Registrar'}
              </span>
            </button>
          </div>
        </div>
      </div>

      {/* Vertical divider */}
      <div className="login-divider-gold" />

      {/* Right panel — image */}
      <div className="login-image-panel">
        <img
          src="/the_witcher_3_gwent.webp"
          alt="Gwent"
          className="login-image-panel__img"
        />
        <div className="login-image-panel__overlay" />
      </div>
    </div>
  )
}
