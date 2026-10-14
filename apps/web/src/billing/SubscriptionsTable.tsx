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

export function SubscriptionsTable({ subscriptions, onEdit, onCancel }: Props) {
  const { t } = useTranslation()

  const discountText = (s: Subscription) => {
    if (s.discountType === null || s.discountValue === null) return null
    const value = s.discountType === 'PERCENT' ? `${s.discountValue}%` : formatMoney(s.discountValue)
    return t('subscriptions.discountOf', { value }) + (s.discountReason ? ` · ${s.discountReason}` : '')
  }

  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th scope="col">{t('subscriptions.col.student')}</th>
            <th scope="col">{t('subscriptions.col.plan')}</th>
            <th scope="col">{t('subscriptions.col.amount')}</th>
            <th scope="col" className="hide-narrow">{t('subscriptions.col.due')}</th>
            <th scope="col">{t('subscriptions.col.situation')}</th>
            <th scope="col" className="col-actions">{t('subscriptions.col.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {subscriptions.map((s) => (
            <tr key={s.id}>
              <td>
                <div className="row-title">{s.student.name}</div>
                {s.student.nickname && <div className="row-sub">“{s.student.nickname}”</div>}
                {s.student.status === 'INACTIVE' && (
                  <span className="pill pill-muted" title={t('subscriptions.pausedHint')}>{t('subscriptions.paused')}</span>
                )}
              </td>
              <td>
                <div>{s.plan.name}</div>
                <div className="row-sub">{t(`plans.interval.${s.plan.intervalMonths}`)}</div>
              </td>
              <td className="nowrap">
                <div className="row-title">{formatMoney(s.amountCents)}</div>
                {discountText(s) && <div className="row-sub">{discountText(s)}</div>}
              </td>
              <td className="hide-narrow nowrap">
                <div>{t('plans.dayOf', { day: s.dueDay })}</div>
                <div className="row-sub">{t('subscriptions.next', { date: formatDate(s.nextDue) })}</div>
              </td>
              <td>
                {s.overdueSince ? (
                  <span className="pill pill-danger" title={t('subscriptions.overdueSince', { date: formatDate(s.overdueSince) })}>
                    {t('subscriptions.overdue', { value: formatMoney(s.overdueCents) })}
                  </span>
                ) : (
                  <span className="pill pill-success">{t('subscriptions.upToDate')}</span>
                )}
              </td>
              <td className="col-actions">
                <span className="row-actions">
                  <button type="button" className="icon-btn" onClick={() => onEdit(s)}
                    aria-label={t('subscriptions.editNamed', { name: s.student.name })} title={t('common.edit')}>
                    <Pencil size={16} aria-hidden />
                  </button>
                  <button type="button" className="icon-btn" onClick={() => onCancel(s)}
                    aria-label={t('subscriptions.cancelNamed', { name: s.student.name })} title={t('subscriptions.cancel')}>
                    <XCircle size={16} aria-hidden />
                  </button>
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
