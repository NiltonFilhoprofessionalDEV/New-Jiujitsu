import { useTranslation } from 'react-i18next'
import { Navigate, NavLink, Outlet } from 'react-router'

import { STAFF_ROLES } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'
import { BottomNav } from '@/layout/BottomNav'
import { STUDENT_NAV } from '@/layout/navItems'
import { Topbar } from '@/layout/Topbar'
import { cn } from '@/lib/utils'

/** Abas no topo, só no computador; no celular as mesmas abas ficam na barra inferior. */
function DesktopTabs() {
  const { t } = useTranslation()
  return (
    <nav aria-label={t('account.navAria')} className="hidden items-center gap-1 min-[861px]:flex">
      {STUDENT_NAV.map(({ to, labelKey, icon: Icon, end }) => (
        <NavLink key={to} to={to} end={end} className={({ isActive }) => cn(
          'inline-flex h-9 items-center gap-2 rounded-full px-4 text-sm font-semibold no-underline transition-colors hover:no-underline',
          isActive ? 'bg-pulse text-white shadow-glow' : 'text-ink-muted hover:bg-ink-2 hover:text-ink-foreground',
        )}>
          <Icon size={17} aria-hidden />
          {t(labelKey)}
        </NavLink>
      ))}
    </nav>
  )
}

/** Moldura da área do aluno: início, biblioteca de vídeos e ajustes da conta. */
export function StudentShell() {
  const { activeMembership } = useAuth()
  if (!activeMembership) return null
  if (STAFF_ROLES.includes(activeMembership.role)) return <Navigate to="/" replace />

  return (
    <div className="student-shell">
      <Topbar>
        <DesktopTabs />
      </Topbar>
      <main className="main">
        <Outlet />
      </main>
      <BottomNav items={STUDENT_NAV} />
    </div>
  )
}
