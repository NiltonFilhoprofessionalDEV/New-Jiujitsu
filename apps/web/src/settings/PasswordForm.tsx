import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'

/** Usado pela equipe e pela área do aluno. */
export function PasswordForm() {
  const { t } = useTranslation()
  const { changePassword } = useAuth()
  const [mismatch, setMismatch] = useState(false)
  const [saved, setSaved] = useState(false)
  const change = useMutation({
    mutationFn: ({ current, next }: { current: string; next: string }) => changePassword(current, next),
  })
  const fieldErrors = change.error ? (problemOf(change.error).fields ?? {}) : {}

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    const next = String(form.get('newPassword') ?? '')
    const mismatched = next !== String(form.get('confirmPassword') ?? '')
    setSaved(false)
    setMismatch(mismatched)
    if (mismatched) return
    change.mutate({ current: String(form.get('currentPassword') ?? ''), next }, {
      onSuccess: () => {
        formElement.reset()
        setSaved(true)
      },
    })
  }

  return (
    <section className="card stack" aria-labelledby="password-title">
      <div>
        <h3 id="password-title">{t('settings.password.title')}</h3>
        <p className="hint">{t('settings.password.hint')}</p>
      </div>
      {change.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, change.error)}</div>}
      {saved && <div className="notice" role="status">{t('settings.password.saved')}</div>}
      <form className="form-grid" onSubmit={onSubmit}>
        <div className="field span-2">
          <label htmlFor="pw-current">{t('settings.password.current')}</label>
          <input id="pw-current" name="currentPassword" type="password" className="input" required maxLength={72}
            autoComplete="current-password" />
        </div>
        <div className="field">
          <label htmlFor="pw-new">{t('settings.password.new')}</label>
          <input id="pw-new" name="newPassword" type="password" className="input" required minLength={8}
            maxLength={72} autoComplete="new-password" aria-invalid={Boolean(fieldErrors.newPassword)} />
          {fieldErrors.newPassword
            ? <span className="field-error">{fieldErrors.newPassword}</span>
            : <span className="hint">{t('auth.passwordHint')}</span>}
        </div>
        <div className="field">
          <label htmlFor="pw-confirm">{t('settings.password.confirm')}</label>
          <input id="pw-confirm" name="confirmPassword" type="password" className="input" required minLength={8}
            maxLength={72} autoComplete="new-password" aria-invalid={mismatch} />
          {mismatch && <span className="field-error">{t('settings.password.mismatch')}</span>}
        </div>
        <div className="form-actions span-2">
          <button type="submit" className="btn btn-primary" disabled={change.isPending}>
            {change.isPending ? t('common.saving') : t('settings.password.submit')}
          </button>
        </div>
      </form>
    </section>
  )
}
