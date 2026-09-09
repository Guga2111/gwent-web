import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { TooltipProvider } from '@/components/ui/tooltip'
import { Toaster } from '@/components/ui/sonner'
import Landing from '@/pages/Landing'
import Login from '@/pages/Login'
import Hub from '@/pages/Hub'
import Game from '@/pages/Game'
import type { ReactNode } from 'react'

function ProtectedRoute({ children }: { children: ReactNode }) {
  const token = useAuthStore((s) => s.token);
  const user = useAuthStore((s) => s.user);
  if (!token || !user) return <Navigate to="/login" replace />
  return children;
}

function CatchAllRedirect() {
  const token = useAuthStore((s) => s.token);
  return <Navigate to={token ? '/hub' : '/'} replace />;
}

export default function App() {
  return (
    <TooltipProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route
            path="/hub"
            element={
              <ProtectedRoute>
                <Hub />
              </ProtectedRoute>
            }
          />
          <Route
            path="/game/:gameId"
            element={
              <ProtectedRoute>
                <Game />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<CatchAllRedirect />} />
        </Routes>
        <Toaster />
      </BrowserRouter>
    </TooltipProvider>
  )
}
