import { useAuthStore } from '@/stores/authStore'
import { useNavigate } from 'react-router-dom'

export default function Profile() {
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 24,
      }}
    >
      <div
        style={{
          padding: '40px 60px',
          borderRadius: 9,
          background: 'linear-gradient(160deg, #241c13, #1a140d)',
          boxShadow:
            'inset 0 0 0 1px rgba(20,12,5,.8), inset 0 0 0 2px rgba(240,205,120,.16), 0 12px 30px rgba(0,0,0,.45)',
          textAlign: 'center',
        }}
      >
        <div
          style={{
            fontFamily: 'var(--font-display)',
            fontWeight: 700,
            fontSize: 26,
            color: 'var(--gold-light)',
            marginBottom: 16,
          }}
        >
          Brasão
        </div>

        <div
          style={{
            fontFamily: 'var(--font-body)',

            fontSize: 14,
            color: 'var(--text-secondary)',
            marginBottom: 24,
          }}
        >
          {user?.email}
        </div>

        <button
          onClick={() => {
            logout()
            navigate('/login')
          }}
          style={{
            padding: '10px 18px',
            borderRadius: 7,
            background: 'rgba(155,44,44,.16)',
            boxShadow: 'inset 0 0 0 1px rgba(184,71,71,.45)',
            color: '#d98a8a',
            cursor: 'pointer',
            fontSize: 13,
            fontWeight: 600,
            fontFamily: 'var(--font-heading)',
            border: 'none',
          }}
        >
          Deixar a taverna (sair)
        </button>
      </div>
    </div>
  )
}
