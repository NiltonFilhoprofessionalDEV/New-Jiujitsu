import { useMutation } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { Link, useLocation, useNavigate, useParams } from 'react-router'

import { AuthLayout } from '@/auth/pages/AuthLayout'
import { useAuth } from '@/auth/useAuth'
import { apiErrorMessage } from '@/lib/apiErrors'
import { PublicNotice } from '@/public/PublicNotice'

import { StaffSignupForm } from './StaffSignupForm'
import { useStaffInviteInfo } from './teamApi'
import type { PublicStaffInviteInfo, StaffSignupInput } from './types'

export function AcceptStaffInvitePage() {
  const { t } = useTranslation()
  const { token = '' } = useParams()
  const { status } = useAuth()
  const { data: info, isLoading, isError } = useStaffInviteInfo(token)

  if (isLoading || status === 'loading') return <div className="auth-screen" aria-busy="true" />
  if (isError || !info) return <PublicNotice title={t('signup.inviteNotFound')} body={t('signup.askNewInvite')} />
  if (info.status !== 'PENDING') {
    const body = info.status === 'ACCEPTED' ? t('signup.inviteUsedHint') : t('signup.askNewInvite')
    return <PublicNotice title={t(`signup.inviteState.${info.status}`)} body={body} />
  }

  return (
    <AuthLayout title={t('signup.inviteTitle', { academy: info.academyName })}
      subtitle={t('team.accept.subtitle', { role: t(`roles.${info.role}`) })}>
      <AcceptBody token={token} info={info} loggedIn={status === 'authenticated'} />
    </AuthLayout>
  )
}

type BodyProps = { token: string; info: PublicStaffInviteInfo; loggedIn: boolean }

function AcceptBody({ token, info, loggedIn }: BodyProps) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const { session, acceptStaffInvite, logout } = useAuth()
  const accept = useMutation({
    mutationFn: (input: StaffSignupInput | null) => acceptStaffInvite(token, input),
    onSuccess: () => navigate('/', { replace: true }),
  })

  if (!loggedIn) {
    return (
      <>
        <StaffSignupForm info={info} pending={accept.isPending} error={accept.error}
          onSubmit={(input) => accept.mutate(input)} />
        <p className="auth-footer">
          {t('team.accept.hasAccount')}{' '}
          <Link to="/login" state={{ from: location.pathname }}>{t('auth.submitLogin')}</Link>
        </p>
      </>
    )
  }

  const email = session?.user.email ?? ''
  const otherEmail = Boolean(info.email) && info.email !== email.toLowerCase()
  return (
    <div className="stack">
      {accept.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, accept.error)}</div>}
      <p className="hint">
        {otherEmail ? t('team.accept.otherEmail', { email, invited: info.email }) : t('team.accept.loggedAs', { email })}
      </p>
      {!otherEmail && (
        <button type="button" className="btn btn-primary btn-block" disabled={accept.isPending}
          onClick={() => accept.mutate(null)}>
          {t('team.accept.join')}
        </button>
      )}
      <button type="button" className="btn btn-block" onClick={() => void logout()}>
        {t('team.accept.useOther')}
      </button>
    </div>
  )
}
