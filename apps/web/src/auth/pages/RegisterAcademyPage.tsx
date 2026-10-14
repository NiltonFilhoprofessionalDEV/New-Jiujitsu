import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router'

import { problemOf } from '@/lib/api'

import { authErrorMessage } from '../authErrors'
import { useAuth } from '../useAuth'
import { AuthLayout } from './AuthLayout'

const FIELDS = [
  { name: 'academyName', labelKey: 'auth.academyName', type: 'text', autoComplete: 'organization' },
  { name: 'name', labelKey: 'auth.yourName', type: 'text', autoComplete: 'name' },
  { name: 'email', labelKey: 'auth.email', type: 'email', autoComplete: 'email' },
  { name: 'password', labelKey: 'auth.password', type: 'password', autoComplete: 'new-password' },
] as const

export function RegisterAcademyPage() {
  const { t } = useTranslation()
  const { registerAcademy } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      await registerAcademy({
        academyName: String(form.get('academyName')),
        name: String(form.get('name')),
        email: String(form.get('email')),
        password: String(form.get('password')),
      })
      navigate('/', { replace: true })
    } catch (err) {
      setError(authErrorMessage(t, err))
      setFieldErrors(problemOf(err).fields ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title={t('auth.registerTitle')} subtitle={t('auth.registerSubtitle')}>
      <form onSubmit={onSubmit} className="field" style={{ gap: 14 }} noValidate>
        {error && (
          <div className="alert alert-error" role="alert">
            {error}
          </div>
        )}
        {FIELDS.map((f) => (
          <div className="field" key={f.name}>
            <label htmlFor={f.name}>{t(f.labelKey)}</label>
            <input
              id={f.name}
              name={f.name}
              type={f.type}
              className="input"
              autoComplete={f.autoComplete}
              required
              minLength={f.name === 'password' ? 8 : undefined}
              aria-invalid={Boolean(fieldErrors[f.name])}
            />
            {f.name === 'password' && !fieldErrors.password && <span className="empty">{t('auth.passwordHint')}</span>}
            {fieldErrors[f.name] && <span className="field-error">{fieldErrors[f.name]}</span>}
          </div>
        ))}
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {t('auth.submitRegister')}
        </button>
      </form>
      <p className="auth-footer">
        {t('auth.hasAccount')} <Link to="/login">{t('auth.submitLogin')}</Link>
      </p>
    </AuthLayout>
  )
}
