import { useQueryClient } from '@tanstack/react-query'
import type { AxiosResponse } from 'axios'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'

import { api, refreshSession, setAccessToken, subscribeSession } from '@/lib/api'
import type { SignupInput } from '@/public/types'
import type { StaffSignupInput } from '@/team/types'

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

  /** Toda troca de sessão descarta o cache: os dados são da academia/usuário anterior. */
  const enter = useCallback(
    async (request: Promise<AxiosResponse<Session>>) => {
      const { data } = await request
      queryClient.clear()
      apply(data)
    },
    [apply, queryClient],
  )

  const login = useCallback(
    (email: string, password: string) => enter(api.post<Session>('/auth/login', { email, password })),
    [enter],
  )

  const registerAcademy = useCallback(
    (input: RegisterAcademyInput) => enter(api.post<Session>('/auth/register-academy', input)),
    [enter],
  )

  const acceptInvite = useCallback(
    (token: string, input: SignupInput) =>
      enter(api.post<Session>(`/public/invites/${encodeURIComponent(token)}/accept`, input)),
    [enter],
  )

  const acceptStaffInvite = useCallback(
    (token: string, input: StaffSignupInput | null) => {
      const path = encodeURIComponent(token)
      return enter(input
        ? api.post<Session>(`/public/staff-invites/${path}/accept`, input)
        : api.post<Session>(`/staff-invites/${path}/accept`))
    },
    [enter],
  )

  const switchAcademy = useCallback(
    (membershipId: string) => enter(api.post<Session>('/auth/switch-academy', { membershipId })),
    [enter],
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
    return {
      status, session, activeMembership, login, registerAcademy, acceptInvite, acceptStaffInvite, switchAcademy, logout,
    }
  }, [status, session, login, registerAcademy, acceptInvite, acceptStaffInvite, switchAcademy, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
