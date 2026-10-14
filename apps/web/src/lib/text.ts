const MARKS = /\p{M}/gu
const SPACES = /\s+/g

/** Minúsculas, sem acentos e com espaços simples, para "joao" achar "João" (mesma regra da API). */
export function fold(text: string): string {
  return text.normalize('NFD').replace(MARKS, '').toLowerCase().trim().replace(SPACES, ' ')
}
