import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useSaveBeltRules } from './graduationsApi'
import type { BeltRule, RuleTarget } from './types'

/** Mesmos limites da API ({@code BeltRuleDtos.RuleInput}). */
const FIELDS = [
  { key: 'minMonths', min: 0, max: 240 },
  { key: 'minTrainings', min: 0, max: 5000 },
  { key: 'trainingsPerDegree', min: 1, max: 1000 },
] as const satisfies readonly { key: keyof RuleTarget; min: number; max: number }[]

type Props = {
  rule: BeltRule
  onCancel: () => void
  onSaved: (belt: string) => void
}

function numberOrNull(form: FormData, name: string): number | null {
  const value = formText(form, name)
  return value === null ? null : Number(value)
}

/** Regra de uma faixa. Campo vazio não entra na conta de quem está pronto para graduar. */
export function BeltRuleForm({ rule, onCancel, onSaved }: Props) {
  const { t } = useTranslation()
  const save = useSaveBeltRules()

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const next = {
      belt: rule.belt,
      minMonths: numberOrNull(form, 'minMonths'),
      minTrainings: numberOrNull(form, 'minTrainings'),
      trainingsPerDegree: numberOrNull(form, 'trainingsPerDegree'),
    }
    if (FIELDS.every(({ key }) => next[key] === rule[key])) {
      onCancel()
      return
    }
    save.mutate([next], { onSuccess: () => onSaved(rule.belt) })
  }

  return (
    <form onSubmit={onSubmit} className="stack">
      {save.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, save.error)}</div>}
      <fieldset className="stack rules-fields" disabled={save.isPending}>
        <legend className="sr-only">{t('beltRules.editNamed', { belt: t(`belts.${rule.belt}`) })}</legend>
        {FIELDS.map(({ key, min, max }) => (
          <div key={key} className="field">
            <label htmlFor={`rule-${key}`}>{t(`beltRules.${key}`)}</label>
            <input id={`rule-${key}`} type="number" inputMode="numeric" className="input" name={key} min={min}
              max={max} defaultValue={rule[key] ?? ''} placeholder={t('beltRules.emptyField')}
              aria-describedby={`rule-${key}-help`} />
            <span id={`rule-${key}-help`} className="hint">{t(`beltRules.help.${key}`)}</span>
          </div>
        ))}
      </fieldset>
      <div className="form-actions">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={save.isPending}>
          {save.isPending ? t('common.saving') : t('beltRules.save')}
        </button>
      </div>
    </form>
  )
}
