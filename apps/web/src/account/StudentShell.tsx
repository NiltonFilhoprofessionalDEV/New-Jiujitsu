import { PlaySquare, UserRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Navigate, NavLink, Outlet } from 'react-router'

import { STAFF_ROLES, STUDENT_HOME } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'
import { Topbar } from '@/layout/Topbar'

const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? 'student-tab active' : 'student-tab')

/** Moldura da área do aluno: barra superior e abas entre a conta e a biblioteca de vídeos. */
export function StudentShell() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  if (!activeMembership) return null
  if (STAFF_ROLES.includes(activeMembership.role)) return <Navigate to="/" replace />

  return (
    <div className="student-shell">
      <Topbar />
      <main className="main">
        <nav className="student-tabs" aria-label={t('account.navAria')}>
          <NavLink to={STUDENT_HOME} end className={navClass}>
            <UserRound size={18} aria-hidden />
            {t('account.tabs.account')}
          </NavLink>
          <NavLink to={`${STUDENT_HOME}/videos`} className={navClass}>
            <PlaySquare size={18} aria-hidden />
            {t('account.tabs.videos')}
          </NavLink>
        </nav>
        <Outlet />
      </main>
    </div>
  )
}
