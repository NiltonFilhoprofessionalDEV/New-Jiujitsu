import { LogOut } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Navigate } from 'react-router'

import { AuthLayout } from '@/auth/pages/AuthLayout'
import { STAFF_ROLES } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'

/** Área do aluno provisória: confirma o vínculo com a academia até as telas do aluno chegarem. */
export function StudentAccountPage() {
  const { t } = useTranslation()
  const { session, activeMembership, logout } = useAuth()

  if (!session || !activeMembership) return null
  if (STAFF_ROLES.includes(activeMembership.role)) return <Navigate to="/" replace />

  return (
    <AuthLayout title={t('account.hello', { name: session.user.name.split(' ')[0] })}
      subtitle={t('account.linked', {
        academy: activeMembership.academy.name,
        role: t(`roles.${activeMembership.role}`),
      })}>
      <p className="notice">{t('account.comingSoon')}</p>
      <button type="button" className="btn btn-block" onClick={() => void logout()}>
        <LogOut size={16} aria-hidden /> {t('topbar.logout')}
      </button>
    </AuthLayout>
  )
}
