import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams } from 'react-router'

import { PasswordField } from '@/components/PasswordField'
import { problemOf } from '@/lib/api'
import { PublicNotice } from '@/public/PublicNotice'

import { authErrorMessage } from '../authErrors'
import { useAuth } from '../useAuth'
import { AuthLayout } from './AuthLayout'
import { useAcademyInviteInfo } from './academyInvite'

const FIELDS = [
  { name: 'academyName', labelKey: 'auth.academyName', type: 'text', autoComplete: 'organization' },
  { name: 'name', labelKey: 'auth.yourName', type: 'text', autoComplete: 'name' },
  { name: 'email', labelKey: 'auth.email', type: 'email', autoComplete: 'email' },
  { name: 'password', labelKey: 'auth.password', type: 'password', autoComplete: 'new-password' },
] as const

export function RegisterAcademyPage() {
  const { t } = useTranslation()
  const { token = '' } = useParams()
  const { status } = useAuth()
  const { data: info, isLoading, isError } = useAcademyInviteInfo(token)

  if (isLoading || status === 'loading') return <div className="auth-screen" aria-busy="true" />
  if (isError || !info) return <PublicNotice title={t('signup.inviteNotFound')} body={t('signup.askNewInvite')} />
  if (info.status !== 'PENDING') {
    const body = info.status === 'ACCEPTED' ? t('signup.inviteUsedHint') : t('signup.askNewInvite')
    return <PublicNotice title={t(`signup.inviteState.${info.status}`)} body={body} />
  }

  return (
    <AuthLayout title={t('auth.registerTitle')} subtitle={t('auth.registerSubtitle')}>
      <RegisterForm token={token} />
    </AuthLayout>
  )
}

function RegisterForm({ token }: { token: string }) {
  const { t } = useTranslation()
  const { registerAcademy } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [confirmOwner, setConfirmOwner] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setError(null)
    setFieldErrors({})
    if (!confirmOwner) {
      setError(t('auth.ownerConfirmRequired'))
      return
    }
    setSubmitting(true)
    try {
      await registerAcademy(token, {
        academyName: String(form.get('academyName')),
        name: String(form.get('name')),
        email: String(form.get('email')),
        password: String(form.get('password')),
        confirmOwner: true,
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
    <>
      <form onSubmit={onSubmit} className="field" style={{ gap: 14 }} noValidate>
        {error && (
          <div className="alert alert-error" role="alert">
            {error}
          </div>
        )}
        {FIELDS.map((f) => (
          <div className="field" key={f.name}>
            <label htmlFor={f.name}>{t(f.labelKey)}</label>
            {f.name === 'password' ? (
              <PasswordField
                id={f.name}
                name={f.name}
                autoComplete={f.autoComplete}
                required
                minLength={8}
                aria-invalid={Boolean(fieldErrors[f.name])}
              />
            ) : (
              <input
                id={f.name}
                name={f.name}
                type={f.type}
                className="input"
                autoComplete={f.autoComplete}
                placeholder={f.name === 'name' ? t('students.form.namePlaceholder') : undefined}
                required
                aria-invalid={Boolean(fieldErrors[f.name])}
              />
            )}
            {f.name === 'password' && !fieldErrors.password && <span className="empty">{t('auth.passwordHint')}</span>}
            {fieldErrors[f.name] && <span className="field-error">{fieldErrors[f.name]}</span>}
          </div>
        ))}
        <label className="checkbox-inline" style={{ width: '100%', alignItems: 'flex-start' }}>
          <input
            type="checkbox"
            checked={confirmOwner}
            onChange={(event) => setConfirmOwner(event.target.checked)}
          />
          {t('auth.ownerConfirm')}
        </label>
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {t('auth.submitRegister')}
        </button>
      </form>
      <p className="auth-footer">
        {t('auth.hasAccount')} <Link to="/login">{t('auth.submitLogin')}</Link>
      </p>
    </>
  )
}
