import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useSavePlan } from './billingApi'
import { centsToInput, formCents, formInt } from './money'
import { INTERVALS, MAX_DUE_DAY } from './terms'
import type { Plan, PlanRequest } from './types'

const MAX_PRICE_REAIS = 100_000
const DEFAULT_DUE_DAY = 10

type Props = { plan?: Plan; onSaved: () => void; onCancel: () => void }

function toRequest(form: FormData): PlanRequest {
  return {
    name: formText(form, 'name') ?? '',
    description: formText(form, 'description'),
    priceCents: formCents(form, 'price'),
    intervalMonths: formInt(form, 'interval') ?? 1,
    dueDay: formInt(form, 'dueDay'),
    active: form.has('active'),
    isDefault: form.has('isDefault'),
    isKidsDefault: form.has('isKidsDefault'),
  }
}

export function PlanForm({ plan, onSaved, onCancel }: Props) {
  const { t } = useTranslation()
  const save = useSavePlan()
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    try {
      await save.mutateAsync({ id: plan?.id, body: toRequest(new FormData(event.currentTarget)) })
      onSaved()
    } catch (err) {
      setError(apiErrorMessage(t, err))
      setFieldErrors(problemOf(err).fields ?? {})
    }
  }

  const fieldError = (name: string) =>
    fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
      <div className="field span-2">
        <label htmlFor="pf-name">{t('plans.form.name')}</label>
        <input id="pf-name" name="name" className="input" required maxLength={80} defaultValue={plan?.name}
          placeholder={t('plans.form.namePlaceholder')} />
        {fieldError('name')}
      </div>
      <div className="field span-2">
        <label htmlFor="pf-description">{t('plans.form.description')}</label>
        <textarea id="pf-description" name="description" className="input" rows={2} maxLength={500}
          defaultValue={plan?.description ?? ''} />
      </div>
      <div className="field">
        <label htmlFor="pf-price">{t('plans.form.price')}</label>
        <input id="pf-price" name="price" type="number" className="input" required min={0} max={MAX_PRICE_REAIS} step="0.01"
          inputMode="decimal" defaultValue={centsToInput(plan?.priceCents)} placeholder="150,00" />
        {fieldError('priceCents')}
      </div>
      <div className="field">
        <label htmlFor="pf-interval">{t('plans.form.interval')}</label>
        <select id="pf-interval" name="interval" className="input" defaultValue={plan?.intervalMonths ?? 1}>
          {INTERVALS.map((n) => <option key={n} value={n}>{t(`plans.interval.${n}`)}</option>)}
        </select>
      </div>
      <div className="field">
        <label htmlFor="pf-due">{t('plans.form.dueDay')}</label>
        <input id="pf-due" name="dueDay" type="number" className="input" required min={1} max={MAX_DUE_DAY}
          defaultValue={plan?.dueDay ?? DEFAULT_DUE_DAY} />
        <span className="hint">{t('plans.form.dueDayHint')}</span>
        {fieldError('dueDay')}
      </div>
      <fieldset className="field span-2 plan-flags">
        <legend>{t('plans.form.flags')}</legend>
        <label className="checkbox-inline">
          <input type="checkbox" name="active" defaultChecked={plan?.active ?? true} /> {t('plans.form.active')}
        </label>
        <label className="checkbox-inline">
          <input type="checkbox" name="isDefault" defaultChecked={plan?.isDefault} /> {t('plans.form.isDefault')}
        </label>
        <label className="checkbox-inline">
          <input type="checkbox" name="isKidsDefault" defaultChecked={plan?.isKidsDefault} />{' '}
          {t('plans.form.isKidsDefault')}
        </label>
        <span className="hint">{t('plans.form.defaultsHint')}</span>
      </fieldset>
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={save.isPending}>
          {save.isPending ? t('common.saving') : plan ? t('common.save') : t('plans.form.submitCreate')}
        </button>
      </div>
    </form>
  )
}
