import { CheckCircle2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useParams } from 'react-router'

import { AuthLayout } from '@/auth/pages/AuthLayout'

import { useEnroll, useEnrollLinkInfo } from './publicApi'
import { PublicNotice } from './PublicNotice'
import { SignupForm } from './SignupForm'

export function EnrollPage() {
  const { t } = useTranslation()
  const { token = '' } = useParams()
  const { data: info, isLoading, isError } = useEnrollLinkInfo(token)
  const enroll = useEnroll(token)

  if (isLoading) return <div className="auth-screen" aria-busy="true" />
  if (isError || !info) return <PublicNotice title={t('signup.linkNotFound')} body={t('signup.askNewLink')} />
  if (info.state !== 'OPEN') {
    return <PublicNotice title={t(`signup.linkState.${info.state}`)} body={t('signup.askNewLink')} />
  }

  if (enroll.isSuccess) {
    return (
      <AuthLayout title={t('signup.sentTitle')} subtitle={info.academyName}>
        <div className="signup-done" role="status">
          <CheckCircle2 size={40} aria-hidden />
          <p>{t('signup.sentBody', { academy: info.academyName })}</p>
          <Link className="btn btn-block" to="/login">{t('auth.submitLogin')}</Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout title={t('signup.enrollTitle', { academy: info.academyName })} subtitle={t('signup.enrollSubtitle')}>
      <SignupForm kind="TRAINING" submitLabel={t('signup.submitEnroll')} pending={enroll.isPending}
        error={enroll.error} onSubmit={(input) => enroll.mutate(input)} />
      <p className="auth-footer">
        {t('auth.hasAccount')} <Link to="/login">{t('auth.submitLogin')}</Link>
      </p>
    </AuthLayout>
  )
}
