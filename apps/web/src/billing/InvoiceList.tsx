import { useTranslation } from 'react-i18next'

import { Pagination } from '@/components/Pagination'
import { formatDate } from '@/students/format'
import type { Page } from '@/students/types'

import { formatMoney } from './money'
import { SITUATION_PILL } from './situation'
import type { Invoice } from './types'

export type RowAction = 'pay' | 'cancel' | 'reverse'

type Props = {
  page: Page<Invoice>
  onPage: (page: number) => void
  onAction: (action: RowAction, invoice: Invoice) => void
}

export function InvoiceList({ page, onPage, onAction }: Props) {
  return (
    <div className="card list-card">
      <ul className="people-list">
        {page.content.map((i) => <InvoiceRow key={i.id} invoice={i} onAction={(a) => onAction(a, i)} />)}
      </ul>
      <Pagination page={page} onPage={onPage} />
    </div>
  )
}

function InvoiceRow({ invoice: i, onAction }: { invoice: Invoice; onAction: (action: RowAction) => void }) {
  const { t } = useTranslation()
  const open = i.situation === 'OPEN' || i.situation === 'OVERDUE'
  const paid = i.paidOn && i.paymentMethod
    ? t('invoices.paidOn', { date: formatDate(i.paidOn), method: t(`invoices.method.${i.paymentMethod}`) })
    : null

  return (
    <li className="list-row">
      <span className="person-main">
        <span className="row-title">{i.studentName}</span>
        <span className="row-sub">{i.description} · {t('invoices.dueOn', { date: formatDate(i.dueDate) })}</span>
      </span>
      <span className="row-money">
        <span className="row-title">{formatMoney(i.amountCents)}</span>
        {paid && <span className="row-sub">{paid}</span>}
      </span>
      <span className={SITUATION_PILL[i.situation]}>{t(`invoices.situation.${i.situation}`)}</span>
      <span className="row-actions">
        {open && (
          <>
            <button type="button" className="btn btn-sm" onClick={() => onAction('pay')}>
              {t('invoices.pay.action')}
            </button>
            <button type="button" className="btn btn-sm btn-ghost" onClick={() => onAction('cancel')}>
              {t('invoices.cancel.action')}
            </button>
          </>
        )}
        {i.situation === 'PAID' && (
          <button type="button" className="btn btn-sm btn-ghost" onClick={() => onAction('reverse')}>
            {t('invoices.reverse.action')}
          </button>
        )}
      </span>
    </li>
  )
}
