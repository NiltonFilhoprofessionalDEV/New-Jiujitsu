import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { PasswordField } from '@/components/PasswordField'
import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import type { PublicStaffInviteInfo, StaffSignupInput } from './types'

type Props = {
  info: PublicStaffInviteInfo
  pending: boolean
  error: unknown
  onSubmit: (input: StaffSignupInput) => void
}

/** Conta nova pelo convite; o e-mail do convite, se houver, fica travado. */
export function StaffSignupForm({ info, pending, error, onSubmit }: Props) {
  const { t } = useTranslation()
  const fieldErrors = error ? (problemOf(error).fields ?? {}) : {}

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    onSubmit({
      name: formText(form, 'name') ?? '',
      email: formText(form, 'email') ?? '',
      password: String(form.get('password') ?? ''),
    })
  }

  const fieldError = (name: string) =>
    fieldErrors[name] ? <span className="field-error">{fieldErrors[name]}</span> : null

  return (
    <form className="form-grid" onSubmit={submit}>
      {Boolean(error) && <div className="alert alert-error span-2" role="alert">{apiErrorMessage(t, error)}</div>}
      <div className="field span-2">
        <label htmlFor="ss-name">{t('signup.name')}</label>
        <input id="ss-name" name="name" className="input" required maxLength={120} autoComplete="name"
          autoCapitalize="words" placeholder={t('students.form.namePlaceholder')} defaultValue={info.name ?? ''}
          aria-invalid={Boolean(fieldErrors.name)} />
        {fieldError('name')}
      </div>
      <div className="field span-2">
        <label htmlFor="ss-email">{t('auth.email')}</label>
        <input id="ss-email" name="email" type="email" className="input" required maxLength={254} autoComplete="email"
          defaultValue={info.email ?? ''} readOnly={Boolean(info.email)} aria-invalid={Boolean(fieldErrors.email)} />
        {fieldError('email')}
      </div>
      <div className="field span-2">
        <label htmlFor="ss-password">{t('auth.password')}</label>
        <PasswordField id="ss-password" name="password" required minLength={8} maxLength={72}
          autoComplete="new-password" aria-invalid={Boolean(fieldErrors.password)} />
        {fieldError('password') ?? <span className="hint">{t('auth.passwordHint')}</span>}
      </div>
      <div className="form-actions span-2">
        <button type="submit" className="btn btn-primary btn-block" disabled={pending}>
          {pending ? t('common.saving') : t('signup.submitInvite')}
        </button>
      </div>
    </form>
  )
}
