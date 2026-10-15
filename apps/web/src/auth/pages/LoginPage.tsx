import { Download } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { InstallApp } from '@/pwa/InstallApp'

import { authErrorMessage } from '../authErrors'
import { useAuth } from '../useAuth'
import { AuthLayout } from './AuthLayout'

export function LoginPage() {
  const { t } = useTranslation()
  const { login } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setSubmitting(true)
    setError(null)
    try {
      await login(String(form.get('email')), String(form.get('password')))
    } catch (err) {
      setError(authErrorMessage(t, err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title={t('auth.loginTitle')} subtitle={t('auth.loginSubtitle')} after={<InstallCta />}>
      <form onSubmit={onSubmit} className="field" style={{ gap: 14 }} noValidate>
        {error && (
          <div className="alert alert-error" role="alert">
            {error}
          </div>
        )}
        <div className="field">
          <label htmlFor="email">{t('auth.email')}</label>
          <input id="email" name="email" type="email" className="input" autoComplete="email" required />
        </div>
        <div className="field">
          <label htmlFor="password">{t('auth.password')}</label>
          <input
            id="password"
            name="password"
            type="password"
            className="input"
            autoComplete="current-password"
            required
          />
        </div>
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {t('auth.submitLogin')}
        </button>
      </form>
      <p className="auth-footer">
        {t('auth.noAccount')} <Link to="/register-academy">{t('auth.registerAcademy')}</Link>
      </p>
    </AuthLayout>
  )
}

function InstallCta() {
  const { t } = useTranslation()
  return (
    <InstallApp>
      {(install) => (
        <button type="button" className="install-cta" onClick={install} title={t('install.ctaHint')}>
          <Download size={15} aria-hidden />
          {t('install.cta')}
        </button>
      )}
    </InstallApp>
  )
}
