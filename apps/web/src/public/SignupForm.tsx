import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'
import { BeltFields } from '@/students/BeltFields'
import type { StudentKind } from '@/students/types'

import type { SignupInput } from './types'

type Props = {
  kind: StudentKind
  /** E-mail definido pelo convite: aparece preenchido e não pode ser trocado. */
  fixedEmail?: string | null
  initialName?: string | null
  submitLabel: string
  pending: boolean
  error: unknown
  onSubmit: (input: SignupInput) => void
}

function toInput(form: FormData, training: boolean): SignupInput {
  return {
    name: formText(form, 'name') ?? '',
    nickname: formText(form, 'nickname'),
    email: formText(form, 'email') ?? '',
    password: String(form.get('password') ?? ''),
    cpf: formText(form, 'cpf'),
    phone: formText(form, 'phone'),
    emergencyPhone: formText(form, 'emergencyPhone'),
    birthDate: formText(form, 'birthDate'),
    belt: training ? formText(form, 'belt') : null,
    degrees: training ? Number(form.get('degrees') ?? 0) : null,
  }
}

export function SignupForm({ kind, fixedEmail, initialName, submitLabel, pending, error, onSubmit }: Props) {
  const { t } = useTranslation()
  const training = kind === 'TRAINING'
  const fieldErrors = error ? (problemOf(error).fields ?? {}) : {}

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onSubmit(toInput(new FormData(event.currentTarget), training))
  }

  const fieldError = (name: string) =>
    fieldErrors[name] ? <span className="field-error">{fieldErrors[name]}</span> : null

  return (
    <form className="form-grid" onSubmit={submit}>
      {Boolean(error) && <div className="alert alert-error span-2" role="alert">{apiErrorMessage(t, error)}</div>}
      <div className="field span-2">
        <label htmlFor="su-name">{t('signup.name')}</label>
        <input id="su-name" name="name" className="input" required maxLength={120} autoComplete="name"
          autoCapitalize="words" defaultValue={initialName ?? ''} aria-invalid={Boolean(fieldErrors.name)} />
        {fieldError('name') ?? <span className="hint">{t('students.form.nameHint')}</span>}
      </div>
      {training && (
        <div className="field span-2">
          <label htmlFor="su-nickname">{t('students.form.nickname')}</label>
          <input id="su-nickname" name="nickname" className="input" maxLength={60} />
        </div>
      )}
      <div className="field span-2">
        <label htmlFor="su-email">{t('students.form.email')}</label>
        <input id="su-email" name="email" type="email" className="input" required maxLength={254} autoComplete="email"
          defaultValue={fixedEmail ?? ''} readOnly={Boolean(fixedEmail)} aria-invalid={Boolean(fieldErrors.email)} />
        {fieldError('email')}
      </div>
      <div className="field span-2">
        <label htmlFor="su-password">{t('auth.password')}</label>
        <input id="su-password" name="password" type="password" className="input" required minLength={8} maxLength={72}
          autoComplete="new-password" aria-invalid={Boolean(fieldErrors.password)} />
        {fieldError('password') ?? <span className="hint">{t('auth.passwordHint')}</span>}
      </div>
      <div className="field">
        <label htmlFor="su-phone">{t('students.form.phone')}</label>
        <input id="su-phone" name="phone" type="tel" className="input" maxLength={30} autoComplete="tel"
          aria-invalid={Boolean(fieldErrors.phone)} />
        {fieldError('phone')}
      </div>
      <div className="field">
        <label htmlFor="su-cpf">{t('students.form.cpf')}</label>
        <input id="su-cpf" name="cpf" className="input" inputMode="numeric" maxLength={14}
          aria-invalid={Boolean(fieldErrors.cpf)} />
        {fieldError('cpf')}
      </div>
      {training && (
        <div className="field span-2">
          <label htmlFor="su-emergency">{t('students.form.emergencyPhone', { context: kind })}</label>
          <input id="su-emergency" name="emergencyPhone" type="tel" className="input" required maxLength={30}
            aria-invalid={Boolean(fieldErrors.emergencyPhone)} />
          {fieldError('emergencyPhone') ?? <span className="hint">{t('students.form.emergencyPhoneHint')}</span>}
        </div>
      )}
      <div className="field span-2">
        <label htmlFor="su-birth">{t('students.form.birthDate')}</label>
        <input id="su-birth" name="birthDate" type="date" className="input" autoComplete="bday" />
      </div>
      {training && <BeltFields idPrefix="su" />}
      <div className="form-actions span-2">
        <button type="submit" className="btn btn-primary btn-block" disabled={pending}>
          {pending ? t('common.saving') : submitLabel}
        </button>
      </div>
    </form>
  )
}
