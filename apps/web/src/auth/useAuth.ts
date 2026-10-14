import { useContext } from 'react'

import { AuthContext } from './AuthContext'
import { MANAGER_ROLES } from './types'

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  return ctx
}

/** Dono ou co-gestor da academia ativa. */
export function useIsManager(): boolean {
  const { activeMembership } = useAuth()
  return Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
}
