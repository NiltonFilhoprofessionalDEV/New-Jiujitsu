import { useTranslation } from 'react-i18next'

import { formatMoney } from './money'
import type { InvoiceList } from './types'

type Props = { data: InvoiceList; onShowOverdue: () => void }

/** Totais do mês filtrado e, acima, o aviso de inadimplência da academia inteira. */
export function InvoiceSummary({ data, onShowOverdue }: Props) {
  const { t } = useTranslation()
  const { totals, overdue } = data
  const stats = [
    { key: 'open', bucket: totals.open },
    { key: 'overdue', bucket: totals.overdue },
    { key: 'paid', bucket: totals.paid },
  ] as const

  return (
    <>
      {overdue.students > 0 && (
        <div className="callout callout-danger" role="status">
          <span>
            {t('invoices.overdueAlert', { count: overdue.students, value: formatMoney(overdue.cents) })}
          </span>
          <button type="button" className="link-btn" onClick={onShowOverdue}>{t('invoices.showOverdue')}</button>
        </div>
      )}
      <div className="stat-grid">
        {stats.map(({ key, bucket }) => (
          <div key={key} className={`card stat stat-${key}`}>
            <strong>{formatMoney(bucket.cents)}</strong>
            <span>{t(`invoices.totals.${key}`, { count: bucket.count })}</span>
          </div>
        ))}
      </div>
    </>
  )
}
