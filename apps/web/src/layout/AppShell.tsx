import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Outlet, useLocation } from 'react-router'

import { useIsManager } from '@/auth/useAuth'
import { usePlatformAdmin } from '@/platform/platformApi'
import { AccessBanner } from '@/subscription/AccessBanner'
import { useAcademyAccess } from '@/subscription/accessApi'
import { SuspendedScreen } from '@/subscription/SuspendedScreen'
import { ADMIN_PATH, SUBSCRIPTION_PATH } from '@/subscription/types'

import { BottomNav } from './BottomNav'
import { navFor, STAFF_TABS } from './navItems'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

/** Telas que continuam abertas com a academia suspensa (a API também as libera). */
const OPEN_WHEN_SUSPENDED = [SUBSCRIPTION_PATH, ADMIN_PATH]

/**
 * Menu lateral fixo no computador; abaixo dele, barra de abas embaixo e o menu completo pelo "Mais":
 * gaveta no tablet, tela cheia no celular.
 */
export function AppShell() {
  const { t } = useTranslation()
  const isManager = useIsManager()
  const { isAdmin } = usePlatformAdmin()
  const { data: access } = useAcademyAccess()
  const { pathname } = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const close = () => setMenuOpen(false)
  const enforced = access?.enforced === true
  const blocked = enforced && access.status === 'SUSPENDED' && !OPEN_WHEN_SUSPENDED.includes(pathname)

  useEffect(() => {
    if (!menuOpen) return
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setMenuOpen(false)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [menuOpen])

  return (
    <div className="shell" data-menu-open={menuOpen}>
      <Topbar />
      <Sidebar groups={navFor(isManager, { isPlatformAdmin: isAdmin, subscriptionEnforced: enforced })} onClose={close} />
      <button type="button" className="scrim" tabIndex={-1} aria-label={t('topbar.closeMenu')} onClick={close} />
      <main className="main">
        {isManager && enforced && <AccessBanner access={access} />}
        {blocked ? <SuspendedScreen isManager={isManager} /> : <Outlet />}
      </main>
      <BottomNav items={STAFF_TABS} onMore={() => setMenuOpen((open) => !open)} moreOpen={menuOpen} onNavigate={close} />
    </div>
  )
}
