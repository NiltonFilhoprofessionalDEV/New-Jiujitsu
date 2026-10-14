import type { MembershipRole } from '@/auth/types'

export type StaffRole = Extract<MembershipRole, 'INSTRUCTOR' | 'CO_MANAGER'>

export const INVITABLE_ROLES: readonly StaffRole[] = ['INSTRUCTOR', 'CO_MANAGER']

export type TeamMember = {
  membershipId: string
  name: string
  email: string
  role: MembershipRole
  joinedAt: string
  lastUsedAt: string | null
  you: boolean
}

export type StaffInvite = {
  id: string
  token: string
  role: StaffRole
  name: string | null
  email: string | null
  expiresAt: string
  createdAt: string
}

export type CreateStaffInviteInput = { role: StaffRole; name: string | null; email: string | null }

export type StaffInviteStatus = 'PENDING' | 'ACCEPTED' | 'REVOKED' | 'EXPIRED'

export type PublicStaffInviteInfo = {
  academyName: string
  role: StaffRole
  name: string | null
  email: string | null
  status: StaffInviteStatus
}

export type StaffSignupInput = { name: string; email: string; password: string }
