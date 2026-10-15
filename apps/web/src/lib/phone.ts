const BRAZIL_DDI = '55'
/** DDD + número (fixo ou celular), sem o código do país. */
const LOCAL_PHONE_DIGITS = [10, 11]

/** Só os dígitos, com o 55 na frente quando o número foi digitado sem código do país. */
export function intlDigits(phone: string | null | undefined): string | null {
  const digits = (phone ?? '').replace(/\D/g, '')
  if (digits.length === 0) return null
  return LOCAL_PHONE_DIGITS.includes(digits.length) ? BRAZIL_DDI + digits : digits
}

export function telUrl(phone: string | null | undefined): string | null {
  const digits = intlDigits(phone)
  return digits && `tel:+${digits}`
}

/** Conversa no WhatsApp; sem telefone, abre a escolha de contato no próprio app. */
export function whatsappUrl(phone: string | null | undefined, message?: string): string {
  const query = message ? `?text=${encodeURIComponent(message)}` : ''
  return `https://wa.me/${intlDigits(phone) ?? ''}${query}`
}
