import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { BeltFields } from './BeltFields'
import { useAddDependent, useSaveStudent } from './studentsApi'
import type { BeltRuler, Gender, StudentDetail, StudentKind, StudentRequest } from './types'

type StudentFormProps = {
  student?: StudentDetail
  /** Preenchido: cadastra como dependente desta família (sempre aluno que treina). */
  householdId?: string
  onSaved: (student: StudentDetail) => void
  onCancel: () => void
}

function toRequest(form: FormData, kind: StudentKind): StudentRequest {
  const training = kind === 'TRAINING'
  return {
    kind,
    name: formText(form, 'name') ?? '',
    nickname: formText(form, 'nickname'),
    email: formText(form, 'email'),
    cpf: formText(form, 'cpf'),
    phone: formText(form, 'phone'),
    emergencyPhone: formText(form, 'emergencyPhone'),
    birthDate: formText(form, 'birthDate'),
    gender: formText(form, 'gender') as Gender | null,
    belt: training ? formText(form, 'belt') : null,
    degrees: training ? Number(form.get('degrees') ?? 0) : null,
    degreeAwardedAt: training ? formText(form, 'degreeAwardedAt') : null,
    beltRuler: (formText(form, 'beltRuler') ?? 'AUTO') as BeltRuler,
    enrollmentDate: formText(form, 'enrollmentDate'),
    giSize: training ? formText(form, 'giSize') : null,
    beltSize: training ? formText(form, 'beltSize') : null,
  }
}

export function StudentForm({ student, householdId, onSaved, onCancel }: StudentFormProps) {
  const { t } = useTranslation()
  const save = useSaveStudent()
  const addDependent = useAddDependent(householdId ?? '')
  const [kind, setKind] = useState<StudentKind>(student?.kind ?? 'TRAINING')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const pending = save.isPending || addDependent.isPending
  const training = kind === 'TRAINING'

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    const body = toRequest(new FormData(event.currentTarget), kind)
    try {
      const saved = householdId
        ? await addDependent.mutateAsync(body)
        : await save.mutateAsync({ id: student?.id, body })
      onSaved(saved)
    } catch (err) {
      setError(apiErrorMessage(t, err))
      setFieldErrors(problemOf(err).fields ?? {})
    }
  }

  const fieldError = (name: string) =>
    fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && (
        <div className="alert alert-error span-2" role="alert">
          {error}
        </div>
      )}

      {!householdId && (
        <fieldset className="segmented span-2">
          <legend className="sr-only">{t('students.form.kind')}</legend>
          {(['TRAINING', 'RESPONSIBLE'] as const).map((k) => (
            <label key={k} className={kind === k ? 'segment active' : 'segment'}>
              <input type="radio" name="kind" value={k} checked={kind === k} onChange={() => setKind(k)} />
              {t(`students.kind.${k}`)}
            </label>
          ))}
        </fieldset>
      )}

      <div className="field span-2">
        <label htmlFor="sf-name">{t('students.form.name')}</label>
        <input id="sf-name" name="name" className="input" required maxLength={120} autoCapitalize="words"
          defaultValue={student?.name} aria-invalid={Boolean(fieldErrors.name)} />
        {fieldError('name') || <span className="hint">{t('students.form.nameHint')}</span>}
      </div>
      <div className="field">
        <label htmlFor="sf-nickname">{t('students.form.nickname')}</label>
        <input id="sf-nickname" name="nickname" className="input" maxLength={60} defaultValue={student?.nickname ?? ''} />
      </div>
      <div className="field">
        <label htmlFor="sf-email">{t('students.form.emailOptional')}</label>
        <input id="sf-email" name="email" type="email" className="input" maxLength={254} defaultValue={student?.email ?? ''} />
        {fieldError('email')}
      </div>
      <div className="field">
        <label htmlFor="sf-cpf">{t('students.form.cpf')}</label>
        <input id="sf-cpf" name="cpf" className="input" inputMode="numeric" placeholder="000.000.000-00" maxLength={14} defaultValue={student?.cpf ?? ''} />
      </div>
      <div className="field">
        <label htmlFor="sf-phone">{t('students.form.phone')}</label>
        <input id="sf-phone" name="phone" type="tel" className="input" maxLength={30} defaultValue={student?.phone ?? ''}
          aria-invalid={Boolean(fieldErrors.phone)} />
        {fieldError('phone')}
      </div>
      <div className="field">
        <label htmlFor="sf-emergency">{t('students.form.emergencyPhone', { context: kind })}</label>
        <input id="sf-emergency" name="emergencyPhone" type="tel" className="input" maxLength={30} required={training}
          defaultValue={student?.emergencyPhone ?? ''} aria-invalid={Boolean(fieldErrors.emergencyPhone)} />
        {fieldError('emergencyPhone') || <span className="hint">{t('students.form.emergencyPhoneHint')}</span>}
      </div>
      <div className="field">
        <label htmlFor="sf-birth">{t('students.form.birthDate')}</label>
        <input id="sf-birth" name="birthDate" type="date" className="input" defaultValue={student?.birthDate ?? ''} />
        {fieldError('birthDate')}
      </div>
      <div className="field">
        <label htmlFor="sf-gender">{t('students.form.gender')}</label>
        <select id="sf-gender" name="gender" className="input" defaultValue={student?.gender ?? ''}>
          <option value="">{t('students.gender.none')}</option>
          <option value="MALE">{t('students.gender.MALE')}</option>
          <option value="FEMALE">{t('students.gender.FEMALE')}</option>
        </select>
      </div>
      <div className="field">
        <label htmlFor="sf-enrollment">{t('students.form.enrollmentDate', { context: kind })}</label>
        <input id="sf-enrollment" name="enrollmentDate" type="date" className="input" defaultValue={student?.enrollmentDate ?? ''} />
        <span className="hint">{t('students.form.enrollmentHint')}</span>
      </div>

      {training && (
        <>
          <div className="section-title span-2">{t('students.form.jiujitsu')}</div>
          <BeltFields idPrefix="sf" initialBelt={student?.belt} initialDegrees={student?.degrees} />
          <div className="field">
            <label htmlFor="sf-degree-at">{t('students.form.degreeAwardedAt')}</label>
            <input id="sf-degree-at" name="degreeAwardedAt" type="date" className="input" defaultValue={student?.degreeAwardedAt ?? ''} />
          </div>
          <div className="field">
            <label htmlFor="sf-ruler">{t('students.form.beltRuler')}</label>
            <select id="sf-ruler" name="beltRuler" className="input" defaultValue={student?.beltRuler ?? 'AUTO'}>
              {(['AUTO', 'KIDS', 'ADULT'] as const).map((r) => (
                <option key={r} value={r}>
                  {t(`students.ruler.${r}`)}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="sf-gi">{t('students.form.giSize')}</label>
            <input id="sf-gi" name="giSize" className="input" maxLength={20} defaultValue={student?.giSize ?? ''} />
          </div>
          <div className="field">
            <label htmlFor="sf-belt-size">{t('students.form.beltSize')}</label>
            <input id="sf-belt-size" name="beltSize" className="input" maxLength={20} defaultValue={student?.beltSize ?? ''} />
          </div>
        </>
      )}

      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>
          {t('common.cancel')}
        </button>
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? t('common.saving') : student ? t('common.save') : t('students.form.submitCreate')}
        </button>
      </div>
    </form>
  )
}
