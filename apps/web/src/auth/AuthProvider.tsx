import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'

import { api, refreshSession, setAccessToken, subscribeSession } from '@/lib/api'
import type { SignupInput } from '@/public/types'

import { AuthContext, type AuthContextValue, type AuthStatus, type RegisterAcademyInput } from './AuthContext'
import type { Session } from './types'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [session, setSession] = useState<Session | null>(null)
  const [status, setStatus] = useState<AuthStatus>('loading')

  const apply = useCallback((next: Session | null) => {
    setAccessToken(next?.accessToken ?? null)
    setSession(next)
    setStatus(next ? 'authenticated' : 'anonymous')
  }, [])

  useEffect(() => {
    subscribeSession(apply)
    void refreshSession()
  }, [apply])

  const login = useCallback(
    async (email: string, password: string) => {
      const { data } = await api.post<Session>('/auth/login', { email, password })
      queryClient.clear()
      apply(data)
    },
    [apply, queryClient],
  )

  const registerAcademy = useCallback(
    async (input: RegisterAcademyInput) => {
      const { data } = await api.post<Session>('/auth/register-academy', input)
      queryClient.clear()
      apply(data)
    },
    [apply, queryClient],
  )

  const acceptInvite = useCallback(
    async (token: string, input: SignupInput) => {
      const { data } = await api.post<Session>(`/public/invites/${encodeURIComponent(token)}/accept`, input)
      queryClient.clear()
      apply(data)
    },
    [apply, queryClient],
  )

  const switchAcademy = useCallback(
    async (membershipId: string) => {
      const { data } = await api.post<Session>('/auth/switch-academy', { membershipId })
      queryClient.clear()
      apply(data)
    },
    [apply, queryClient],
  )

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      queryClient.clear()
      apply(null)
    }
  }, [apply, queryClient])

  const value = useMemo<AuthContextValue>(() => {
    const activeMembership = session?.memberships.find((m) => m.id === session.activeMembershipId) ?? null
    return { status, session, activeMembership, login, registerAcademy, acceptInvite, switchAcademy, logout }
  }, [status, session, login, registerAcademy, acceptInvite, switchAcademy, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
