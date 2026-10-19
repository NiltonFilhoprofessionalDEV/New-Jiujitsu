import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useSearchParams } from 'react-router'

import { PasswordField } from '@/components/PasswordField'
import { api } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'

import { AuthLayout } from './AuthLayout'

export function ResetPasswordPage() {
  const { t } = useTranslation()
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''
  const [error, setError] = useState<string | null>(null)
  const [mismatch, setMismatch] = useState(false)
  const [done, setDone] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const password = String(form.get('password') ?? '')
    const mismatched = password !== String(form.get('confirmPassword') ?? '')
    setMismatch(mismatched)
    if (mismatched) return
    setSubmitting(true)
    setError(null)
    try {
      await api.post('/auth/reset-password', { token, password })
      setDone(true)
    } catch (err) {
      setError(apiErrorMessage(t, err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title={t('auth.resetTitle')} subtitle={t('auth.resetSubtitle')}>
      {done ? (
        <p className="hint">{t('auth.resetDone')}</p>
      ) : (
        <form onSubmit={onSubmit} className="field" style={{ gap: 14 }} noValidate>
          {error && <div className="alert alert-error" role="alert">{error}</div>}
          {!token && <div className="alert alert-error" role="alert">{t('errors.INVALID_RESET_TOKEN')}</div>}
          <div className="field">
            <label htmlFor="reset-password">{t('auth.password')}</label>
            <PasswordField id="reset-password" name="password" required minLength={8} maxLength={72}
              autoComplete="new-password" disabled={!token} />
            <span className="hint">{t('auth.passwordHint')}</span>
          </div>
          <div className="field">
            <label htmlFor="reset-confirm">{t('settings.password.confirm')}</label>
            <PasswordField id="reset-confirm" name="confirmPassword" required minLength={8} maxLength={72}
              autoComplete="new-password" disabled={!token} aria-invalid={mismatch} />
            {mismatch && <span className="field-error">{t('settings.password.mismatch')}</span>}
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={submitting || !token}>
            {t('auth.resetSubmit')}
          </button>
        </form>
      )}
      <p className="auth-footer">
        <Link to="/login">{t('auth.backToLogin')}</Link>
      </p>
    </AuthLayout>
  )
}
