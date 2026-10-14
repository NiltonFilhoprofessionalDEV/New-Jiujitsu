import { formText } from '@/lib/formText'

import { formCents, formInt } from './money'
import type { DiscountType, TermsRequest } from './types'

/** Periodicidades oferecidas na tela, em meses. */
export const INTERVALS = [1, 2, 3, 6, 12] as const
/** Até 28: o dia existe em todos os meses. */
export const MAX_DUE_DAY = 28
export const MAX_PERCENT = 100

/** O valor do desconto é inteiro no percentual e em reais (convertido para centavos) no valor fixo. */
export function toTerms(form: FormData): TermsRequest {
  const type = formText(form, 'discountType') as DiscountType | null
  const value = type === 'PERCENT' ? formInt(form, 'discountValue')
    : type === 'FIXED' ? formCents(form, 'discountValue') : null
  return {
    planId: formText(form, 'planId') ?? '',
    dueDay: formInt(form, 'dueDay'),
    discountType: type,
    discountValue: value,
    discountReason: type ? formText(form, 'discountReason') : null,
  }
}
