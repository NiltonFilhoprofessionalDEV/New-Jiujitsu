import { Pencil, XCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { formatDate } from '@/students/format'

import { formatMoney } from './money'
import type { Subscription } from './types'

type Props = {
  subscriptions: Subscription[]
  onEdit: (s: Subscription) => void
  onCancel: (s: Subscription) => void
}

export function SubscriptionList({ subscriptions, onEdit, onCancel }: Props) {
  return (
    <div className="card list-card">
      <ul className="people-list">
        {subscriptions.map((s) => (
          <SubscriptionRow key={s.id} subscription={s} onEdit={() => onEdit(s)} onCancel={() => onCancel(s)} />
        ))}
      </ul>
    </div>
  )
}

type RowProps = { subscription: Subscription; onEdit: () => void; onCancel: () => void }

function SubscriptionRow({ subscription: s, onEdit, onCancel }: RowProps) {
  const { t } = useTranslation()
  const discount = s.discountType !== null && s.discountValue !== null
    ? t('subscriptions.discountOf', {
      value: s.discountType === 'PERCENT' ? `${s.discountValue}%` : formatMoney(s.discountValue),
    }) + (s.discountReason ? ` · ${s.discountReason}` : '')
    : null

  return (
    <li className="list-row">
      <span className="person-main">
        <span className="person-name">
          <span className="row-title">{s.student.name}</span>
          {s.student.nickname && <span className="person-nick">“{s.student.nickname}”</span>}
          {s.student.status === 'INACTIVE' && (
            <span className="pill pill-muted" title={t('subscriptions.pausedHint')}>{t('subscriptions.paused')}</span>
          )}
        </span>
        <span className="row-sub">
          {s.plan.name} · {t('subscriptions.next', { date: formatDate(s.nextDue) })}
        </span>
      </span>
      <span className="row-money">
        <span className="row-title">{formatMoney(s.amountCents)}</span>
        <span className="row-sub">{discount ?? t(`plans.interval.${s.plan.intervalMonths}`)}</span>
      </span>
      {s.overdueSince ? (
        <span className="pill pill-danger" title={t('subscriptions.overdueSince', { date: formatDate(s.overdueSince) })}>
          {t('subscriptions.overdue', { value: formatMoney(s.overdueCents) })}
        </span>
      ) : (
        <span className="pill pill-success">{t('subscriptions.upToDate')}</span>
      )}
      <span className="row-actions">
        <button type="button" className="icon-btn" onClick={onEdit}
          aria-label={t('subscriptions.editNamed', { name: s.student.name })} title={t('common.edit')}>
          <Pencil size={16} aria-hidden />
        </button>
        <button type="button" className="icon-btn" onClick={onCancel}
          aria-label={t('subscriptions.cancelNamed', { name: s.student.name })} title={t('subscriptions.cancel')}>
          <XCircle size={16} aria-hidden />
        </button>
      </span>
    </li>
  )
}
