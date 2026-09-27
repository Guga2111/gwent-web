import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, register } from '@/api/auth'
import { useAuthStore } from '@/stores/authStore'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Eye, EyeOff } from 'lucide-react'
import { toast } from 'sonner'
import axios from 'axios'

export default function Login() {
  const [isRegister, setIsRegister] = useState(false)
  const [email, setEmail] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)

  const setToken = useAuthStore((s) => s.setToken)
  const navigate = useNavigate()

  const passwordRules = [
    { label: 'Mínimo 8 caracteres', test: (p: string) => p.length >= 8 },
    { label: 'Uma letra maiúscula', test: (p: string) => /[A-Z]/.test(p) },
    { label: 'Uma letra minúscula', test: (p: string) => /[a-z]/.test(p) },
    { label: 'Um número', test: (p: string) => /\d/.test(p) },
    { label: 'Um caractere especial', test: (p: string) => /[^A-Za-z0-9]/.test(p) },
  ]

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)

    try {
      if (isRegister) {
        await register({ email, username, password })
      }
      const token = await login({ email, password })
      setToken(token)
      navigate('/hub')
    } catch (err: unknown) {
      let message = 'Falha na autenticação'
      if (axios.isAxiosError(err)) {
        const data = err.response?.data
        if (typeof data === 'string') {
          message = data
        } else if (data?.fields && typeof data.fields === 'object') {
          message = Object.values(data.fields).join('. ')
        } else {
          message = data?.message ?? message
        }
      } else if (err instanceof Error) {
        message = err.message
      }
      toast.error(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      {/* Left panel — login form */}
      <div className="login-panel">
        <img src="/gwent-logo.svg" alt="Gwent Online" className="login-brand-logo" />

        <div className="max-w-[380px] w-full flex flex-col gap-6 px-6">
          <h1 className="font-display text-2xl font-bold text-gold-light text-center">
            {isRegister ? 'Registre Seu Brasão' : 'Entrar no Gwent'}
          </h1>
          <p className="font-body text-sm text-text-muted text-center -mt-3">
            {isRegister
              ? 'Crie sua conta para entrar no jogo'
              : 'Entre com suas credenciais para jogar'}
          </p>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email" className="font-heading text-[11px] font-semibold uppercase tracking-[1.5px] text-text-secondary">Email</Label>
              <Input
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
                <Label htmlFor="username" className="font-heading text-[11px] font-semibold uppercase tracking-[1.5px] text-text-secondary">Nome de Usuário</Label>
                <Input
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
              <Label htmlFor="password" className="font-heading text-[11px] font-semibold uppercase tracking-[1.5px] text-text-secondary">Senha</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="form-input pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-secondary transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              </div>
              {isRegister && password.length > 0 && (
                <ul className="flex flex-col gap-0.5 mt-1">
                  {passwordRules.map((rule) => (
                    <li
                      key={rule.label}
                      className={`font-ui text-[11px] flex items-center gap-1.5 ${rule.test(password) ? 'text-green' : 'text-text-muted'}`}
                    >
                      <span>{rule.test(password) ? '✓' : '○'}</span>
                      {rule.label}
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <Button type="submit" disabled={loading} variant="cta" className="w-full py-3 rounded-lg text-sm tracking-widest uppercase disabled:opacity-50 disabled:cursor-not-allowed">
              {loading ? '...' : isRegister ? 'Registrar' : 'Entrar'}
            </Button>
          </form>

          <div className="flex items-center gap-4 before:content-[''] before:flex-1 before:h-px before:bg-border-subtle after:content-[''] after:flex-1 after:h-px after:bg-border-subtle">
            <span className="font-body text-[13px] text-text-muted">ou</span>
          </div>

          <div className="text-center">
            <Button
              type="button"
              variant="ghost"
              onClick={() => setIsRegister(!isRegister)}
              className="bg-none border-none font-body text-sm text-text-secondary cursor-pointer hover:bg-transparent hover:text-gold-light"
            >
              {isRegister
                ? 'Já tem uma conta? '
                : 'Não tem uma conta? '}
              <span className="text-gold underline underline-offset-2 hover:text-gold-light">
                {isRegister ? 'Entrar' : 'Registrar'}
              </span>
            </Button>
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
