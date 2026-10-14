import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { todayIso } from '@/classes/dates'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useInvoiceAction } from './billingApi'
import { formatMoney } from './money'
import type { Invoice, PaymentMethod } from './types'

const METHODS: readonly PaymentMethod[] = ['PIX', 'CASH', 'CARD', 'TRANSFER', 'OTHER']

type Props = { invoice: Invoice; onDone: () => void; onCancel: () => void }

/** Baixa manual: o gestor informa quando e como recebeu. */
export function PayForm({ invoice, onDone, onCancel }: Props) {
  const { t } = useTranslation()
  const action = useInvoiceAction()
  const [error, setError] = useState<string | null>(null)
  const today = todayIso()

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    const form = new FormData(event.currentTarget)
    try {
      await action.mutateAsync({
        type: 'pay',
        id: invoice.id,
        paidOn: formText(form, 'paidOn') ?? today,
        method: (formText(form, 'method') ?? 'PIX') as PaymentMethod,
      })
      onDone()
    } catch (err) {
      setError(apiErrorMessage(t, err))
    }
  }

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
      <p className="dialog-text span-2">
        {t('invoices.pay.summary', { name: invoice.studentName, value: formatMoney(invoice.amountCents) })}
        <br />
        <span className="empty">{invoice.description}</span>
      </p>
      <div className="field">
        <label htmlFor="pay-date">{t('invoices.pay.date')}</label>
        <input id="pay-date" name="paidOn" type="date" className="input" required max={today} defaultValue={today} />
      </div>
      <div className="field">
        <label htmlFor="pay-method">{t('invoices.pay.method')}</label>
        <select id="pay-method" name="method" className="input" defaultValue="PIX">
          {METHODS.map((m) => <option key={m} value={m}>{t(`invoices.method.${m}`)}</option>)}
        </select>
      </div>
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={action.isPending}>
          {action.isPending ? t('common.saving') : t('invoices.pay.submit')}
        </button>
      </div>
    </form>
  )
}
