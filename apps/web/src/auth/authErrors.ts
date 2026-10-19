import type { TFunction } from 'i18next'

import { problemOf } from '@/lib/api'

const KNOWN_CODES = [
  'INVALID_CREDENTIALS',
  'TOO_MANY_ATTEMPTS',
  'EMAIL_IN_USE',
  'NO_ACTIVE_MEMBERSHIP',
  'OWNER_CONFIRMATION_REQUIRED',
  'INVITE_NOT_FOUND',
  'INVITE_NOT_AVAILABLE',
  'REGISTRATION_CLOSED',
  'VALIDATION_ERROR',
  'PERSON_NAME_REQUIRED',
]

export function authErrorMessage(t: TFunction, error: unknown): string {
  const { code } = problemOf(error)
  return code && KNOWN_CODES.includes(code) ? t(`auth.errors.${code}`) : t('auth.errors.generic')
}
