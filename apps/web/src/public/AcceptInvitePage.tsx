import { useMutation } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams } from 'react-router'

import { AuthLayout } from '@/auth/pages/AuthLayout'
import { useAuth } from '@/auth/useAuth'

import { useInviteInfo } from './publicApi'
import { PublicNotice } from './PublicNotice'
import { SignupForm } from './SignupForm'
import type { SignupInput } from './types'

export function AcceptInvitePage() {
  const { t } = useTranslation()
  const { token = '' } = useParams()
  const navigate = useNavigate()
  const { acceptInvite } = useAuth()
  const { data: info, isLoading, isError } = useInviteInfo(token)
  const accept = useMutation({
    mutationFn: (input: SignupInput) => acceptInvite(token, input),
    onSuccess: () => navigate('/', { replace: true }),
  })

  if (isLoading) return <div className="auth-screen" aria-busy="true" />
  if (isError || !info) return <PublicNotice title={t('signup.inviteNotFound')} body={t('signup.askNewInvite')} />
  if (info.status !== 'PENDING') {
    const body = info.status === 'ACCEPTED' ? t('signup.inviteUsedHint') : t('signup.askNewInvite')
    return <PublicNotice title={t(`signup.inviteState.${info.status}`)} body={body} />
  }

  return (
    <AuthLayout title={t('signup.inviteTitle', { academy: info.academyName })}
      subtitle={t('signup.inviteSubtitle', { context: info.kind })}>
      <SignupForm kind={info.kind} fixedEmail={info.email} initialName={info.name}
        submitLabel={t('signup.submitInvite')} pending={accept.isPending} error={accept.error}
        onSubmit={(input) => accept.mutate(input)} />
    </AuthLayout>
  )
}
