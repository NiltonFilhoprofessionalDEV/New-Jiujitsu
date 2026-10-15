import type { StudentKind } from '@/students/types'

export type LinkState = 'OPEN' | 'PAUSED' | 'EXPIRED'

export type EnrollmentLink = {
  token: string
  enabled: boolean
  expiresOn: string | null
  opens: number
  state: LinkState
}

export type InviteStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'REVOKED'
export type InviteTab = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'ALL'

export type Invite = {
  id: string
  token: string
  kind: StudentKind
  name: string | null
  email: string | null
  phone: string | null
  status: InviteStatus
  expiresAt: string
  opens: number
  createdAt: string
}

export type CreateInviteInput = {
  kind: StudentKind
  name: string | null
  email: string | null
  phone: string | null
}

export type EnrollmentRequestItem = {
  id: string
  name: string
  nickname: string | null
  email: string
  cpf: string | null
  phone: string | null
  emergencyContact: string | null
  emergencyPhone: string | null
  birthDate: string | null
  belt: string | null
  degrees: number
  createdAt: string
}
