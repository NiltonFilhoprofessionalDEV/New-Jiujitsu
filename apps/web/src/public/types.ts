import type { InviteStatus, LinkState } from '@/invites/types'
import type { StudentKind } from '@/students/types'

export type SignupInput = {
  name: string
  nickname: string | null
  email: string
  password: string
  cpf: string | null
  phone: string | null
  emergencyPhone: string | null
  birthDate: string | null
  belt: string | null
  degrees: number | null
}

export type PublicLinkInfo = { academyName: string; state: LinkState }

export type PublicInviteInfo = {
  academyName: string
  kind: StudentKind
  name: string | null
  email: string | null
  status: InviteStatus
}
