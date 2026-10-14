export type BeltGroup = 'kids' | 'adult'

export type BeltInfo = {
  slug: string
  group: BeltGroup
  minDegrees: number
  maxDegrees: number
  /** Cor principal e, nas faixas de duas cores, a cor da listra central. */
  colors: [string, string?]
}

/** Tamanho do ícone onde a faixa é o destaque (ficha e área do aluno). */
export const BELT_HERO_SIZE = 56

const WHITE = '#f4f4f5'
const BLACK = '#0a0a0b'
const RED = '#dc2626'
const GREY = '#9ca3af'
const YELLOW = '#facc15'
const ORANGE = '#f97316'
const GREEN = '#16a34a'

/** Mesmos slugs e limites de grau da API (enum {@code Belt}). */
export const BELTS: readonly BeltInfo[] = [
  { slug: 'branca', group: 'adult', minDegrees: 0, maxDegrees: 4, colors: [WHITE] },
  { slug: 'cinza-e-branca', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [GREY, WHITE] },
  { slug: 'cinza', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [GREY] },
  { slug: 'cinza-e-preta', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [GREY, BLACK] },
  { slug: 'amarela-e-branca', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [YELLOW, WHITE] },
  { slug: 'amarela', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [YELLOW] },
  { slug: 'amarela-e-preta', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [YELLOW, BLACK] },
  { slug: 'laranja-e-branca', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [ORANGE, WHITE] },
  { slug: 'laranja', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [ORANGE] },
  { slug: 'laranja-e-preta', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [ORANGE, BLACK] },
  { slug: 'verde-e-branca', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [GREEN, WHITE] },
  { slug: 'verde', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [GREEN] },
  { slug: 'verde-e-preta', group: 'kids', minDegrees: 0, maxDegrees: 4, colors: [GREEN, BLACK] },
  { slug: 'azul', group: 'adult', minDegrees: 0, maxDegrees: 4, colors: ['#2563eb'] },
  { slug: 'roxa', group: 'adult', minDegrees: 0, maxDegrees: 4, colors: ['#7c3aed'] },
  { slug: 'marrom', group: 'adult', minDegrees: 0, maxDegrees: 4, colors: ['#78350f'] },
  { slug: 'preta', group: 'adult', minDegrees: 0, maxDegrees: 6, colors: [BLACK] },
  { slug: 'vermelha-e-preta', group: 'adult', minDegrees: 7, maxDegrees: 7, colors: [RED, BLACK] },
  { slug: 'vermelha-e-branca', group: 'adult', minDegrees: 8, maxDegrees: 8, colors: [RED, WHITE] },
  { slug: 'vermelha', group: 'adult', minDegrees: 9, maxDegrees: 10, colors: [RED] },
]

export function beltInfo(slug: string | null | undefined): BeltInfo | undefined {
  return BELTS.find((b) => b.slug === slug)
}

export function degreeOptions(slug: string): number[] {
  const belt = beltInfo(slug)
  if (!belt) return [0]
  return Array.from({ length: belt.maxDegrees - belt.minDegrees + 1 }, (_, i) => belt.minDegrees + i)
}

export function ageFrom(birthDate: string | null, today = new Date()): number | null {
  if (!birthDate) return null
  const [y, m, d] = birthDate.split('-').map(Number)
  if (!y || !m || !d) return null
  const hadBirthday = today.getMonth() + 1 > m || (today.getMonth() + 1 === m && today.getDate() >= d)
  return today.getFullYear() - y - (hadBirthday ? 0 : 1)
}
