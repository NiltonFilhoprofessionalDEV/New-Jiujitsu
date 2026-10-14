const BRAZIL_DDI = '55'
const LOCAL_PHONE_DIGITS = [10, 11]

export function enrollUrl(token: string): string {
  return `${window.location.origin}/matricula/${token}`
}

export function inviteUrl(token: string): string {
  return `${window.location.origin}/convite/${token}`
}

/** Link do WhatsApp; sem telefone válido, abre a escolha de contato no próprio app. */
export function whatsappUrl(phone: string | null, message: string): string {
  const text = `text=${encodeURIComponent(message)}`
  const digits = (phone ?? '').replace(/\D/g, '')
  if (digits.length === 0) return `https://wa.me/?${text}`
  const full = LOCAL_PHONE_DIGITS.includes(digits.length) ? BRAZIL_DDI + digits : digits
  return `https://wa.me/${full}?${text}`
}
