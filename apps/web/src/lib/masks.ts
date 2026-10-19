function digits(value: string, max: number): string {
  return value.replace(/\D/g, '').slice(0, max)
}

/** `000.000.000-00` */
export function maskCpf(value: string): string {
  const d = digits(value, 11)
  if (d.length <= 3) return d
  if (d.length <= 6) return `${d.slice(0, 3)}.${d.slice(3)}`
  if (d.length <= 9) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`
}

/** `00.000.000/0000-00` */
export function maskCnpj(value: string): string {
  const d = digits(value, 14)
  if (d.length <= 2) return d
  if (d.length <= 5) return `${d.slice(0, 2)}.${d.slice(2)}`
  if (d.length <= 8) return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5)}`
  if (d.length <= 12) return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8)}`
  return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-${d.slice(12)}`
}

/** CPF até 11 dígitos; a partir do 12º, CNPJ. */
export function maskCpfCnpj(value: string): string {
  return digits(value, 14).length > 11 ? maskCnpj(value) : maskCpf(value)
}

/** Celular `(11) 99999-9999` ou fixo `(11) 9999-9999`. */
export function maskPhone(value: string): string {
  const d = digits(value, 11)
  if (d.length === 0) return ''
  if (d.length < 3) return `(${d}`
  const head = `(${d.slice(0, 2)}) `
  const rest = d.slice(2)
  if (d.length <= 10) {
    if (rest.length <= 4) return `${head}${rest}`
    return `${head}${rest.slice(0, 4)}-${rest.slice(4)}`
  }
  return `${head}${rest.slice(0, 5)}-${rest.slice(5)}`
}

/** Mostra um telefone já salvo (com ou sem máscara) no formato brasileiro. */
export function formatPhone(raw: string | null | undefined): string | null {
  if (!raw) return null
  const count = raw.replace(/\D/g, '').length
  if (count < 10 || count > 11) return raw
  return maskPhone(raw)
}
