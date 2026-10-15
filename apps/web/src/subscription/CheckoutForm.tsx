import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useCheckout } from './accessApi'
import type { AcademyAccess } from './types'

/** Só segue para páginas https: a URL vem do serviço de pagamento, através da API. */
function openPaymentPage(url: string): boolean {
  try {
    if (new URL(url).protocol !== 'https:') return false
  } catch {
    return false
  }
  window.location.assign(url)
  return true
}

/**
 * Primeira vez: CPF/CNPJ de quem paga e "Assinar". Depois, abre a fatura em aberto.
 * O pagamento (Pix, boleto ou cartão) acontece na página do Asaas.
 */
export function CheckoutForm({ access }: { access: AcademyAccess }) {
  const { t } = useTranslation()
  const checkout = useCheckout()
  const [invalidUrl, setInvalidUrl] = useState(false)

  if (!access.onlinePayment) {
    return <div className="notice"><p>{t('subscription.checkout.unavailable')}</p></div>
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setInvalidUrl(false)
    const taxId = access.subscribed ? null : formText(new FormData(event.currentTarget), 'cpfCnpj')
    checkout.mutate(taxId, { onSuccess: ({ invoiceUrl }) => setInvalidUrl(!openPaymentPage(invoiceUrl)) })
  }

  const busy = checkout.isPending || (checkout.isSuccess && !invalidUrl)
  return (
    <form className="checkout-form" onSubmit={onSubmit}>
      {checkout.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, checkout.error)}</div>}
      {invalidUrl && <div className="alert alert-error" role="alert">{t('errors.generic')}</div>}
      {!access.subscribed && (
        <div className="field">
          <label htmlFor="checkout-tax-id">{t('subscription.checkout.taxId')}</label>
          <input id="checkout-tax-id" name="cpfCnpj" className="input" required maxLength={20}
            autoComplete="off" placeholder={t('subscription.checkout.taxIdPlaceholder')} />
          <span className="hint">{t('subscription.checkout.taxIdHint')}</span>
        </div>
      )}
      <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
        {busy ? t('subscription.checkout.opening')
          : access.subscribed ? t('subscription.checkout.payOpen') : t('subscription.checkout.subscribe')}
      </button>
      <p className="hint">{t('subscription.checkout.methods')}</p>
    </form>
  )
}
