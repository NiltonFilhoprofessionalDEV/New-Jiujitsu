import { formText } from '@/lib/formText'

const CENTS_PER_REAL = 100
const BRL = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/** A API trabalha com centavos inteiros; a tela, com reais. */
export function formatMoney(cents: number): string {
  return BRL.format(cents / CENTS_PER_REAL)
}

/** Valor para um {@code <input type="number" step="0.01">}. */
export function centsToInput(cents: number | null | undefined): string {
  return cents === null || cents === undefined ? '' : (cents / CENTS_PER_REAL).toFixed(2)
}

/** Lê um campo numérico em reais; vazio ou inválido vira {@code null}. */
export function formCents(form: FormData, key: string): number | null {
  const text = formText(form, key)
  if (text === null) return null
  const reais = Number(text)
  return Number.isFinite(reais) ? Math.round(reais * CENTS_PER_REAL) : null
}

/** Lê um campo inteiro; vazio ou inválido vira {@code null}. */
export function formInt(form: FormData, key: string): number | null {
  const text = formText(form, key)
  if (text === null) return null
  const value = Number(text)
  return Number.isInteger(value) ? value : null
}
