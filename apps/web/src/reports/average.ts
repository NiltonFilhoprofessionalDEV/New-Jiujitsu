const ONE_DECIMAL = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 })

/** Presenças por aula dada; "—" quando nenhuma aula teve presença. */
export function perSession(checkIns: number, sessions: number): string {
  return sessions === 0 ? '—' : ONE_DECIMAL.format(checkIns / sessions)
}
