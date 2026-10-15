import { Phone, Siren } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

import { WhatsappIcon } from '@/components/WhatsappIcon'
import { telUrl, whatsappUrl } from '@/lib/phone'

import type { StudentListItem } from './types'

const ICON_SIZE = 17

type ContactLink = { key: string; href: string | null; label: string; icon: ReactNode; className?: string; external?: boolean }

/**
 * Atalhos de contato da linha do aluno. Cada ícone tem posição fixa (o vazio ocupa o lugar do que falta),
 * para os ícones e a faixa ficarem alinhados em todas as linhas.
 */
export function StudentContactLinks({ student }: { student: StudentListItem }) {
  const { t } = useTranslation()
  const name = student.name
  const call = telUrl(student.phone)
  const links: ContactLink[] = [
    { key: 'whatsapp', href: call && whatsappUrl(student.phone), label: t('students.contact.whatsapp', { name }),
      icon: <WhatsappIcon size={ICON_SIZE} />, className: 'whatsapp', external: true },
    { key: 'call', href: call, label: t('students.contact.call', { name }),
      icon: <Phone size={ICON_SIZE} aria-hidden /> },
    { key: 'emergency', href: telUrl(student.emergencyPhone),
      label: student.emergencyContact
        ? t('students.contact.emergencyNamed', { name, contact: student.emergencyContact })
        : t('students.contact.emergency', { name }),
      icon: <Siren size={ICON_SIZE} aria-hidden />, className: 'emergency' },
  ]

  return (
    <div className="contact-links" role="group" aria-label={t('students.contact.aria', { name })}>
      {links.map((link) => link.href
        ? (
          <a key={link.key} className={`icon-btn contact-link ${link.className ?? ''}`} href={link.href}
            aria-label={link.label} title={link.label}
            {...(link.external && { target: '_blank', rel: 'noopener noreferrer' })}>
            {link.icon}
          </a>
        )
        : <span key={link.key} className="contact-slot" aria-hidden />)}
    </div>
  )
}
