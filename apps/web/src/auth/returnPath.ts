/** Destino depois do login. Só caminhos internos: {@code //host} levaria para fora do app. */
export function returnPath(state: unknown): string {
  const from = (state as { from?: unknown } | null)?.from
  return typeof from === 'string' && from.startsWith('/') && !from.startsWith('//') ? from : '/'
}
