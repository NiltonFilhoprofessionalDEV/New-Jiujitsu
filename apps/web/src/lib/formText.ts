/** Valor textual de um campo do formulário, sem espaços nas pontas; vazio vira {@code null}. */
export function formText(form: FormData, key: string): string | null {
  const value = String(form.get(key) ?? '').trim()
  return value === '' ? null : value
}
