const MARKS = /\p{M}/gu
const SPACES = /\s+/g

/** Minúsculas, sem acentos e com espaços simples, para "joao" achar "João" (mesma regra da API). */
export function fold(text: string): string {
  return text.normalize('NFD').replace(MARKS, '').toLowerCase().trim().replace(SPACES, ' ')
}

/** Iniciais do primeiro e do último nome, para avatares ("Nilton Souza Cabral" → "NC"). */
export function initials(name: string): string {
  const parts = name.trim().split(SPACES).filter(Boolean)
  const firstLetter = (word: string | undefined) => (word ? Array.from(word)[0] ?? '' : '')
  const last = parts.length > 1 ? firstLetter(parts.at(-1)) : ''
  return `${firstLetter(parts[0])}${last}`.toUpperCase()
}
