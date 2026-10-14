import { Navigate, Outlet, useLocation } from 'react-router'

import { returnPath } from './returnPath'
import { STAFF_ROLES, STUDENT_HOME } from './types'
import { useAuth, useIsManager } from './useAuth'

export function RequireAuth() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <div className="auth-screen" aria-busy="true" />
  if (status === 'anonymous') return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}

/** Ao autenticar (login/cadastro), volta para a página que pediu o login. */
export function RedirectIfAuthenticated() {
  const { status } = useAuth()
  const location = useLocation()
  if (status === 'loading') return <div className="auth-screen" aria-busy="true" />
  if (status === 'authenticated') return <Navigate to={returnPath(location.state)} replace />
  return <Outlet />
}

/** Painel de gestão só para equipe; aluno e responsável caem na área do aluno. */
export function RequireStaff() {
  const { activeMembership } = useAuth()
  const staff = Boolean(activeMembership && STAFF_ROLES.includes(activeMembership.role))
  return staff ? <Outlet /> : <Navigate to={STUDENT_HOME} replace />
}

/** Telas só de gestor (ex.: financeiro); o professor volta ao início. A API também nega o acesso. */
export function RequireManager() {
  return useIsManager() ? <Outlet /> : <Navigate to="/" replace />
}
