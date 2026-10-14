import { useTranslation } from 'react-i18next'

import { formatMoney } from '@/billing/money'
import { SITUATION_PILL } from '@/billing/situation'
import { formatDate } from '@/students/format'

import { usePortalInvoices } from './portalApi'

export function InvoicesCard({ studentId }: { studentId: string }) {
  const { t } = useTranslation()
  const { data: invoices, isLoading, isError } = usePortalInvoices(studentId)
  return (
    <section className="card stack" aria-labelledby="invoices-title">
      <div>
        <h3 id="invoices-title">{t('account.invoices.title')}</h3>
        <p className="hint">{t('account.invoices.hint')}</p>
      </div>
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('account.loadError')}</div>}
      {invoices?.length === 0 && <p className="empty">{t('account.invoices.empty')}</p>}
      {invoices && invoices.length > 0 && (
        <ul className="plain-list">
          {invoices.map((i) => (
            <li key={i.id}>
              <div>
                <div className="row-title">{i.description}</div>
                <div className="row-sub">
                  {i.paidOn
                    ? t('account.invoices.paidOn', { date: formatDate(i.paidOn) })
                    : t('account.invoices.dueOn', { date: formatDate(i.dueDate) })}
                </div>
              </div>
              <div className="invoice-side">
                <strong>{formatMoney(i.amountCents)}</strong>
                <span className={SITUATION_PILL[i.situation]}>{t(`invoices.situation.${i.situation}`)}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
