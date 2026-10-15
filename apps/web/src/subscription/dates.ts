const DATE = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })

/** Instantes da API (ISO com hora) no fuso de quem está vendo. */
export function formatInstantDate(iso: string | null): string | null {
  return iso ? DATE.format(new Date(iso)) : null
}
