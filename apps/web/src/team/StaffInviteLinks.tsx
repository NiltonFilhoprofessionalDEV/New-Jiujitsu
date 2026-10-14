import { MessageCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { CopyButton } from '@/components/CopyButton'
import { whatsappUrl } from '@/invites/shareLinks'

import { staffInviteUrl } from './teamApi'
import type { StaffInvite } from './types'

/** Copiar o link ou mandar pelo WhatsApp (sem telefone: a pessoa escolhe o contato no app). */
export function StaffInviteLinks({ invite }: { invite: StaffInvite }) {
  const { t } = useTranslation()
  const url = staffInviteUrl(invite.token)
  const greeting = invite.name ? t('invites.waGreetingNamed', { name: invite.name }) : t('invites.waGreeting')
  const message = t('team.invite.waMessage', { greeting, role: t(`roles.${invite.role}`), link: url })
  return (
    <div className="link-actions">
      <CopyButton text={url} label={t('invites.copy')} />
      <a className="btn btn-sm" target="_blank" rel="noreferrer" href={whatsappUrl(null, message)}>
        <MessageCircle size={15} aria-hidden /> {t('invites.sendWa')}
      </a>
    </div>
  )
}
