import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { todayIso } from '@/classes/dates'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { Dialog } from '@/components/Dialog'
import { apiErrorMessage } from '@/lib/apiErrors'
import { useDebouncedValue } from '@/lib/useDebouncedValue'

import { useInvoiceAction, useInvoices } from './billingApi'
import { InvoicesTable, type RowAction } from './InvoicesTable'
import { InvoiceSummary } from './InvoiceSummary'
import { formatMoney } from './money'
import { PayForm } from './PayForm'
import type { Invoice, InvoiceQuery, SituationFilter } from './types'

const SITUATIONS: readonly SituationFilter[] = ['all', 'open', 'overdue', 'paid', 'canceled']
const currentMonth = () => todayIso().slice(0, 7)

type Modal = { action: RowAction; invoice: Invoice } | null
type Filters = Omit<InvoiceQuery, 'q'>

export function InvoicesPage() {
  const { t } = useTranslation()
  const [filters, setFilters] = useState<Filters>(() => ({ situation: 'all', month: currentMonth(), page: 0 }))
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebouncedValue(search)
  const query: InvoiceQuery = { ...filters, q: debouncedSearch }
  const { data, isLoading, isError } = useInvoices(query)
  const action = useInvoiceAction()
  const [modal, setModal] = useState<Modal>(null)
  const update = (patch: Partial<Filters>) => setFilters((f) => ({ ...f, page: 0, ...patch }))
  const close = () => {
    setModal(null)
    action.reset()
  }

  async function onConfirm() {
    if (!modal || modal.action === 'pay') return
    await action.mutateAsync({ type: modal.action, id: modal.invoice.id })
    close()
  }

  const confirming = modal && modal.action !== 'pay' ? modal : null

  return (
    <div className="page page-wide">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('nav.groups.financeiro')}</div>
          <h1>{t('invoices.title')}</h1>
        </div>
      </div>

      <div className="stack">
        <p className="hint">{t('invoices.intro')}</p>
        {data && <InvoiceSummary data={data} onShowOverdue={() => update({ situation: 'overdue', month: '' })} />}
        <div className="toolbar">
          <input type="search" className="input grow" value={search}
            onChange={(e) => { setSearch(e.target.value); update({ page: 0 }) }}
            placeholder={t('invoices.search')} aria-label={t('invoices.search')} />
          <input type="month" className="input" value={query.month} aria-label={t('invoices.month')}
            onChange={(e) => update({ month: e.target.value })} />
          {query.month && (
            <button type="button" className="btn btn-sm btn-ghost" onClick={() => update({ month: '' })}>
              {t('invoices.allMonths')}
            </button>
          )}
        </div>
        <div className="chips" role="tablist" aria-label={t('invoices.situationAria')}>
          {SITUATIONS.map((s) => (
            <button key={s} type="button" role="tab" aria-selected={query.situation === s}
              className={query.situation === s ? 'chip active' : 'chip'} onClick={() => update({ situation: s })}>
              {t(`invoices.filter.${s}`)}
            </button>
          ))}
        </div>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('invoices.loadError')}</div>}
        {data && data.page.totalElements === 0 && <div className="card empty-detail">{t('invoices.empty')}</div>}
        {data && data.page.totalElements > 0 && (
          <InvoicesTable page={data.page} onPage={(page) => setFilters((f) => ({ ...f, page }))}
            onAction={(a, invoice) => setModal({ action: a, invoice })} />
        )}
      </div>

      <Dialog open={modal?.action === 'pay'} onClose={close} title={t('invoices.pay.title')}>
        {modal?.action === 'pay' && <PayForm key={modal.invoice.id} invoice={modal.invoice} onDone={close} onCancel={close} />}
      </Dialog>
      <ConfirmDialog
        open={confirming !== null}
        title={confirming ? t(`invoices.${confirming.action}.title`) : ''}
        body={confirming ? t(`invoices.${confirming.action}.confirm`, {
          name: confirming.invoice.studentName, value: formatMoney(confirming.invoice.amountCents),
        }) : ''}
        confirmLabel={confirming ? t(`invoices.${confirming.action}.title`) : ''}
        pending={action.isPending}
        error={action.error ? apiErrorMessage(t, action.error) : null}
        onConfirm={() => void onConfirm().catch(() => undefined)}
        onClose={close}
      />
    </div>
  )
}
