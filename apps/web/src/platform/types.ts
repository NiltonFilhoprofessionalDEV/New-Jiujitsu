import type { AccessStatus } from '@/subscription/types'

export type GrantKind = 'TRIAL_EXTENSION' | 'MANUAL_PAYMENT'

export const GRANT_KINDS: readonly GrantKind[] = ['TRIAL_EXTENSION', 'MANUAL_PAYMENT']

/** Limites iguais aos da API. */
export const GRANT_MAX_DAYS = 366
export const GRANT_NOTE_MAX = 200

export type AdminAcademy = {
  id: string
  name: string
  slug: string
  createdAt: string
  ownerName: string | null
  ownerEmail: string | null
  activeStudents: number
  subscribed: boolean
  trialEndsAt: string
  paidUntil: string | null
  status: AccessStatus
  accessUntil: string
  suspendsAt: string
  daysLeft: number
}

export type GrantRequest = { kind: GrantKind; days: number; note: string | null }
