import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'
import { BeltBadge } from '@/students/BeltBadge'
import { beltInfo, type BeltGroup } from '@/students/belts'

import type { useSaveBeltRules } from './graduationsApi'
import type { BeltRule, RuleTarget } from './types'

/** Mesmos limites da API ({@code BeltRuleDtos.RuleInput}). */
const FIELDS = [
  { key: 'minMonths', min: 0, max: 240 },
  { key: 'minTrainings', min: 0, max: 5000 },
  { key: 'trainingsPerDegree', min: 1, max: 1000 },
] as const satisfies readonly { key: keyof RuleTarget; min: number; max: number }[]

const GROUPS: readonly BeltGroup[] = ['adult', 'kids']

type Props = {
  rules: BeltRule[]
  canEdit: boolean
  /** Vem do pai: o formulário remonta com os dados salvos e o aviso de "salvo" precisa sobreviver. */
  save: ReturnType<typeof useSaveBeltRules>
}

function numberOrNull(form: FormData, name: string): number | null {
  const value = formText(form, name)
  return value === null ? null : Number(value)
}

/** Só envia as faixas alteradas: as demais continuam no padrão e seguem eventuais ajustes dele. */
function changedRules(form: FormData, rules: BeltRule[]) {
  return rules
    .map((rule) => ({
      belt: rule.belt,
      minMonths: numberOrNull(form, `${rule.belt}.minMonths`),
      minTrainings: numberOrNull(form, `${rule.belt}.minTrainings`),
      trainingsPerDegree: numberOrNull(form, `${rule.belt}.trainingsPerDegree`),
    }))
    .filter((next, i) => FIELDS.some(({ key }) => next[key] !== rules[i]?.[key]))
}

export function BeltRulesForm({ rules, canEdit, save }: Props) {
  const { t } = useTranslation()

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const changed = changedRules(new FormData(event.currentTarget), rules)
    if (changed.length > 0) save.mutate(changed)
  }

  return (
    <form onSubmit={onSubmit} className="stack" onChange={() => save.isSuccess && save.reset()}>
      {save.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, save.error)}</div>}
      {save.isSuccess && <div className="alert alert-success" role="status">{t('beltRules.saved')}</div>}
      <fieldset className="card table-card rules-fieldset" disabled={!canEdit || save.isPending}>
        <legend className="sr-only">{t('beltRules.title')}</legend>
        <table className="table rules-table">
          <thead>
            <tr>
              <th>{t('beltRules.belt')}</th>
              {FIELDS.map(({ key }) => <th key={key}>{t(`beltRules.${key}`)}</th>)}
            </tr>
          </thead>
          {GROUPS.map((group) => (
            <tbody key={group}>
              <tr className="rules-group"><th colSpan={FIELDS.length + 1}>{t(`belts.group.${group}`)}</th></tr>
              {rules.filter((r) => beltInfo(r.belt)?.group === group).map((rule) => (
                <tr key={rule.belt}>
                  <td>
                    <BeltBadge belt={rule.belt} degrees={0} />
                    {rule.custom && <span className="pill pill-outline rules-custom">{t('beltRules.custom')}</span>}
                  </td>
                  {FIELDS.map(({ key, min, max }) => (
                    <td key={key}>
                      <input type="number" className="input rules-input" name={`${rule.belt}.${key}`} min={min}
                        max={max} defaultValue={rule[key] ?? ''} placeholder="—"
                        aria-label={`${t(`beltRules.${key}`)} · ${t(`belts.${rule.belt}`)}`} />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          ))}
        </table>
      </fieldset>
      <p className="hint">{t('beltRules.blackBelt')}</p>
      <p className="hint">{t('beltRules.defaultsNote')}</p>
      {canEdit && (
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={save.isPending}>
            {save.isPending ? t('common.saving') : t('beltRules.save')}
          </button>
        </div>
      )}
    </form>
  )
}
