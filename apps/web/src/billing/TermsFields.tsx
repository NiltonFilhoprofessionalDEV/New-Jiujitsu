import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { centsToInput } from './money'
import { MAX_DUE_DAY, MAX_PERCENT } from './terms'
import type { DiscountType, Plan, Subscription } from './types'

type Props = { plans: Plan[]; subscription?: Subscription }

/** Campos de plano, vencimento e desconto, comuns a vincular e editar. Planos inativos só aparecem se já em uso. */
export function TermsFields({ plans, subscription: s }: Props) {
  const { t } = useTranslation()
  const [discountType, setDiscountType] = useState<DiscountType | ''>(s?.discountType ?? '')
  const options = plans.filter((p) => p.active || p.id === s?.plan.id)
  const percent = discountType === 'PERCENT'

  return (
    <>
      <div className="field">
        <label htmlFor="tf-plan">{t('subscriptions.form.plan')}</label>
        <select id="tf-plan" name="planId" className="input" required defaultValue={s?.plan.id ?? options[0]?.id}>
          {options.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name} · {t(`plans.interval.${p.intervalMonths}`)}
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <label htmlFor="tf-due">{t('subscriptions.form.dueDay')}</label>
        <input id="tf-due" name="dueDay" type="number" className="input" min={1} max={MAX_DUE_DAY}
          defaultValue={s?.dueDay ?? ''} placeholder={t('subscriptions.form.planDay')} />
      </div>
      <div className="field">
        <label htmlFor="tf-discount-type">{t('subscriptions.form.discountType')}</label>
        <select id="tf-discount-type" name="discountType" className="input" value={discountType}
          onChange={(e) => setDiscountType(e.target.value as DiscountType | '')}>
          <option value="">{t('subscriptions.discount.none')}</option>
          <option value="PERCENT">{t('subscriptions.discount.PERCENT')}</option>
          <option value="FIXED">{t('subscriptions.discount.FIXED')}</option>
        </select>
      </div>
      {discountType && (
        <div className="field">
          <label htmlFor="tf-discount-value">
            {percent ? t('subscriptions.form.discountPercent') : t('subscriptions.form.discountAmount')}
          </label>
          <input key={discountType} id="tf-discount-value" name="discountValue" type="number" className="input"
            required min={0} max={percent ? MAX_PERCENT : undefined} step={percent ? 1 : 0.01}
            defaultValue={s?.discountType === discountType
              ? (percent ? s.discountValue ?? '' : centsToInput(s.discountValue)) : ''} />
        </div>
      )}
      {discountType && (
        <div className="field span-2">
          <label htmlFor="tf-discount-reason">{t('subscriptions.form.discountReason')}</label>
          <input id="tf-discount-reason" name="discountReason" className="input" maxLength={120}
            defaultValue={s?.discountReason ?? ''} placeholder={t('subscriptions.form.discountReasonPlaceholder')} />
        </div>
      )}
    </>
  )
}
