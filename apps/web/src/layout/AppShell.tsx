import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Outlet } from 'react-router'

import { useIsManager } from '@/auth/useAuth'

import { navFor } from './navItems'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

/** Menu lateral fixo no computador; no celular vira gaveta aberta pela barra superior. */
export function AppShell() {
  const { t } = useTranslation()
  const isManager = useIsManager()
  const [menuOpen, setMenuOpen] = useState(false)
  const close = () => setMenuOpen(false)

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
      <Topbar onToggleMenu={() => setMenuOpen((open) => !open)} />
      <Sidebar groups={navFor(isManager)} onNavigate={close} />
      <button type="button" className="scrim" tabIndex={-1} aria-label={t('topbar.closeMenu')} onClick={close} />
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
