import { useTranslation } from 'react-i18next'

import { Pagination } from '@/components/Pagination'
import { formatDate } from '@/students/format'
import type { Page } from '@/students/types'

import { formatMoney } from './money'
import type { Invoice, InvoiceSituation } from './types'

export type RowAction = 'pay' | 'cancel' | 'reverse'

const SITUATION_PILL: Record<InvoiceSituation, string> = {
  OPEN: 'pill pill-outline',
  OVERDUE: 'pill pill-danger',
  PAID: 'pill pill-success',
  CANCELED: 'pill pill-muted',
}

type Props = {
  page: Page<Invoice>
  onPage: (page: number) => void
  onAction: (action: RowAction, invoice: Invoice) => void
}

export function InvoicesTable({ page, onPage, onAction }: Props) {
  const { t } = useTranslation()
  const open = (i: Invoice) => i.situation === 'OPEN' || i.situation === 'OVERDUE'

  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th scope="col">{t('invoices.col.student')}</th>
            <th scope="col">{t('invoices.col.due')}</th>
            <th scope="col">{t('invoices.col.amount')}</th>
            <th scope="col">{t('invoices.col.situation')}</th>
            <th scope="col" className="col-actions">{t('invoices.col.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {page.content.map((i) => (
            <tr key={i.id}>
              <td>
                <div className="row-title">{i.studentName}</div>
                <div className="row-sub">{i.description}</div>
              </td>
              <td className="nowrap">{formatDate(i.dueDate)}</td>
              <td className="nowrap">{formatMoney(i.amountCents)}</td>
              <td>
                <span className={SITUATION_PILL[i.situation]}>{t(`invoices.situation.${i.situation}`)}</span>
                {i.paidOn && i.paymentMethod && (
                  <div className="row-sub">
                    {t('invoices.paidOn', { date: formatDate(i.paidOn), method: t(`invoices.method.${i.paymentMethod}`) })}
                  </div>
                )}
              </td>
              <td className="col-actions">
                <span className="row-actions">
                  {open(i) && (
                    <>
                      <button type="button" className="btn btn-sm" onClick={() => onAction('pay', i)}>
                        {t('invoices.pay.action')}
                      </button>
                      <button type="button" className="btn btn-sm btn-ghost" onClick={() => onAction('cancel', i)}>
                        {t('invoices.cancel.action')}
                      </button>
                    </>
                  )}
                  {i.situation === 'PAID' && (
                    <button type="button" className="btn btn-sm btn-ghost" onClick={() => onAction('reverse', i)}>
                      {t('invoices.reverse.action')}
                    </button>
                  )}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={page} onPage={onPage} />
    </div>
  )
}
