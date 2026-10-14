import type { TFunction } from 'i18next'

import { problemOf } from '@/lib/api'

const KNOWN_CODES = ['INVALID_CREDENTIALS', 'TOO_MANY_ATTEMPTS', 'EMAIL_IN_USE', 'NO_ACTIVE_MEMBERSHIP', 'VALIDATION_ERROR']

export function authErrorMessage(t: TFunction, error: unknown): string {
  const { code } = problemOf(error)
  return code && KNOWN_CODES.includes(code) ? t(`auth.errors.${code}`) : t('auth.errors.generic')
}
