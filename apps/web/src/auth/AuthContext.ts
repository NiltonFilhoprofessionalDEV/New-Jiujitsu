import { createContext } from 'react'

import type { SignupInput } from '@/public/types'

import type { MembershipSummary, Session } from './types'

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

export type RegisterAcademyInput = {
  academyName: string
  name: string
  email: string
  password: string
}

export type AuthContextValue = {
  status: AuthStatus
  session: Session | null
  activeMembership: MembershipSummary | null
  login: (email: string, password: string) => Promise<void>
  registerAcademy: (input: RegisterAcademyInput) => Promise<void>
  /** Aceita o convite e já entra na academia com a nova conta. */
  acceptInvite: (token: string, input: SignupInput) => Promise<void>
  switchAcademy: (membershipId: string) => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
