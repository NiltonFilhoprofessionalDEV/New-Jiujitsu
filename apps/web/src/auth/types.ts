export type MembershipRole = 'OWNER' | 'CO_MANAGER' | 'INSTRUCTOR' | 'STUDENT' | 'RESPONSIBLE'

export type MembershipSummary = {
  id: string
  role: MembershipRole
  academy: { id: string; name: string; slug: string }
  platformSupport: boolean
}

export type Session = {
  accessToken: string
  expiresIn: number
  user: { id: string; name: string; email: string }
  activeMembershipId: string
  memberships: MembershipSummary[]
}

export const MANAGER_ROLES: readonly MembershipRole[] = ['OWNER', 'CO_MANAGER']

/** Papéis que usam o painel de gestão; aluno e responsável vão para a área do aluno. */
export const STAFF_ROLES: readonly MembershipRole[] = ['OWNER', 'CO_MANAGER', 'INSTRUCTOR']

export const STUDENT_HOME = '/minha-conta'
