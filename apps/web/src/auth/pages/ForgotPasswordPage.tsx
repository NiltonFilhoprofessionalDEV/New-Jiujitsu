import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { api } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'

import { AuthLayout } from './AuthLayout'

export function ForgotPasswordPage() {
  const { t } = useTranslation()
  const [error, setError] = useState<string | null>(null)
  const [sent, setSent] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const email = String(new FormData(event.currentTarget).get('email') ?? '')
    setSubmitting(true)
    setError(null)
    try {
      await api.post('/auth/forgot-password', { email })
      setSent(true)
    } catch (err) {
      setError(apiErrorMessage(t, err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title={t('auth.forgotTitle')} subtitle={t('auth.forgotSubtitle')}>
      {sent ? (
        <p className="hint">{t('auth.forgotSent')}</p>
      ) : (
        <form onSubmit={onSubmit} className="field" style={{ gap: 14 }} noValidate>
          {error && <div className="alert alert-error" role="alert">{error}</div>}
          <div className="field">
            <label htmlFor="email">{t('auth.email')}</label>
            <input id="email" name="email" type="email" className="input" autoComplete="email" required />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
            {t('auth.forgotSubmit')}
          </button>
        </form>
      )}
      <p className="auth-footer">
        <Link to="/login">{t('auth.backToLogin')}</Link>
      </p>
    </AuthLayout>
  )
}
