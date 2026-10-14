export function formatCpf(digits: string | null): string | null {
  if (!digits || digits.length !== 11) return digits
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`
}

/** Datas da API vêm como {@code yyyy-MM-dd}; formata sem passar por fuso horário. */
export function formatDate(iso: string | null): string | null {
  if (!iso) return null
  const [y, m, d] = iso.slice(0, 10).split('-')
  return y && m && d ? `${d}/${m}/${y}` : iso
}
