import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { todayIso } from '@/classes/dates'
import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'
import { BeltFields } from '@/students/BeltFields'
import { BELTS, beltInfo, degreeOptions } from '@/students/belts'

import { useRegisterGraduation } from './graduationsApi'
import type { GraduationKind, GraduationRequest } from './types'

type Props = {
  student: { id: string; belt: string | null; degrees: number }
  suggestedKind: GraduationKind | null
  onSaved: () => void
  onCancel: () => void
}

const KINDS: readonly GraduationKind[] = ['BELT', 'DEGREE']
const MAX_PRIOR_TRAININGS = 10_000
const MAX_NOTE = 500

/** Próxima faixa na mesma régua (infantil ou adulto); sem próxima, mantém a atual para o gestor escolher. */
function nextBelt(slug: string): string {
  const current = beltInfo(slug)
  if (!current) return slug
  const after = BELTS.slice(BELTS.indexOf(current) + 1)
  return after.find((b) => b.group === current.group)?.slug ?? slug
}

function toRequest(form: FormData, kind: GraduationKind): GraduationRequest {
  const prior = formText(form, 'priorTrainings')
  return {
    kind,
    belt: kind === 'BELT' ? formText(form, 'belt') : null,
    degrees: Number(formText(form, 'degrees') ?? 0),
    graduatedOn: formText(form, 'graduatedOn') ?? '',
    priorTrainings: prior === null ? null : Number(prior),
    exception: form.get('exception') === 'on',
    note: formText(form, 'note'),
  }
}

export function GraduationForm({ student, suggestedKind, onSaved, onCancel }: Props) {
  const { t } = useTranslation()
  const register = useRegisterGraduation(student.id)
  const currentBelt = student.belt ?? 'branca'
  const nextDegrees = degreeOptions(currentBelt).filter((d) => d > student.degrees)
  const [kind, setKind] = useState<GraduationKind>(suggestedKind ?? (nextDegrees.length > 0 ? 'DEGREE' : 'BELT'))
  const [exception, setException] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const degreeChoices = exception ? degreeOptions(currentBelt) : nextDegrees

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFieldErrors({})
    try {
      await register.mutateAsync(toRequest(new FormData(event.currentTarget), kind))
      onSaved()
    } catch (err) {
      setFieldErrors(problemOf(err).fields ?? {})
    }
  }

  const fieldError = (name: string) =>
    fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {register.error && (
        <div className="alert alert-error span-2" role="alert">
          {apiErrorMessage(t, register.error)}
        </div>
      )}
      <fieldset className="segmented span-2">
        <legend className="sr-only">{t('graduation.form.kind')}</legend>
        {KINDS.map((k) => (
          <label key={k} className={kind === k ? 'segment active' : 'segment'}>
            <input type="radio" name="kind" value={k} checked={kind === k} onChange={() => setKind(k)} />
            {t(`graduation.kind.${k}`)}
          </label>
        ))}
      </fieldset>

      {kind === 'BELT' ? (
        <BeltFields idPrefix="gf" initialBelt={nextBelt(currentBelt)} />
      ) : (
        <div className="field span-2">
          <label htmlFor="gf-degree">{t('graduation.form.newDegree', { belt: t(`belts.${currentBelt}`) })}</label>
          {degreeChoices.length > 0 ? (
            <select key={String(exception)} id="gf-degree" name="degrees" className="input">
              {degreeChoices.map((d) => (
                <option key={d} value={d}>{t('students.degree', { count: d })}</option>
              ))}
            </select>
          ) : (
            <p className="hint">{t('graduation.form.maxDegree')}</p>
          )}
        </div>
      )}

      <div className="field">
        <label htmlFor="gf-date">{t('graduation.form.date')}</label>
        <input id="gf-date" name="graduatedOn" type="date" className="input" required max={todayIso()}
          defaultValue={todayIso()} />
        {fieldError('graduatedOn')}
      </div>
      <div className="field">
        <label htmlFor="gf-prior">{t('graduation.form.priorTrainings')}</label>
        <input id="gf-prior" name="priorTrainings" type="number" className="input" min={0}
          max={MAX_PRIOR_TRAININGS} />
        {fieldError('priorTrainings')}
      </div>
      <div className="field span-2">
        <label htmlFor="gf-note">{t('graduation.form.note')}</label>
        <textarea id="gf-note" name="note" className="input" rows={2} maxLength={MAX_NOTE} />
      </div>
      <label className="checkbox-inline span-2">
        <input type="checkbox" name="exception" checked={exception} onChange={(e) => setException(e.target.checked)} />
        <span>{t('graduation.form.exception')}</span>
      </label>

      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>
          {t('common.cancel')}
        </button>
        <button type="submit" className="btn btn-primary" disabled={register.isPending}>
          {register.isPending ? t('common.saving') : t('graduation.register')}
        </button>
      </div>
    </form>
  )
}
