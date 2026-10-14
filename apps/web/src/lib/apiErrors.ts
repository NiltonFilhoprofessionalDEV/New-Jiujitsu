import type { TFunction } from 'i18next'

import { problemOf } from './api'

/** Traduz o {@code code} do ProblemDetail pela chave {@code errors.<CODE>}, com mensagem genérica como fallback. */
export function apiErrorMessage(t: TFunction, error: unknown): string {
  const { code } = problemOf(error)
  const generic = t('errors.generic')
  return code ? t(`errors.${code}`, { defaultValue: generic }) : generic
}
