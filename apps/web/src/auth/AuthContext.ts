import { createContext } from 'react'

import type { SignupInput } from '@/public/types'
import type { StaffSignupInput } from '@/team/types'

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
  /** Convite de equipe: {@code input} cria a conta; {@code null} aceita com a conta já logada. */
  acceptStaffInvite: (token: string, input: StaffSignupInput | null) => Promise<void>
  switchAcademy: (membershipId: string) => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
