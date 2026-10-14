import { ChevronRight, Inbox } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { usePendingRequestCount } from '@/invites/invitesApi'

/** Fila de matrículas aguardando aprovação do gestor. */
export function RequestsCard() {
  const { t } = useTranslation()
  const { data: pending } = usePendingRequestCount(true)
  const waiting = (pending ?? 0) > 0
  const status = () => {
    if (pending === undefined) return t('common.loading')
    return waiting ? t('home.queue.waiting', { count: pending }) : t('home.queue.empty')
  }

  return (
    <Link to="/requests" className={waiting ? 'card queue-card waiting' : 'card queue-card'}>
      <span className="queue-icon" aria-hidden>
        <Inbox size={18} />
      </span>
      <span className="live-main">
        <strong>{t('home.queue.title')}</strong>
        <span className="hint">{status()}</span>
      </span>
      <ChevronRight size={18} aria-hidden className="live-chevron" />
    </Link>
  )
}
