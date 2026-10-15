import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Outlet } from 'react-router'

import { useIsManager } from '@/auth/useAuth'

import { BottomNav } from './BottomNav'
import { navFor, STAFF_TABS } from './navItems'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

/**
 * Menu lateral fixo no computador; abaixo dele, barra de abas embaixo e o menu completo pelo "Mais":
 * gaveta no tablet, tela cheia no celular.
 */
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
      <Topbar />
      <Sidebar groups={navFor(isManager)} onClose={close} />
      <button type="button" className="scrim" tabIndex={-1} aria-label={t('topbar.closeMenu')} onClick={close} />
      <main className="main">
        <Outlet />
      </main>
      <BottomNav items={STAFF_TABS} onMore={() => setMenuOpen((open) => !open)} moreOpen={menuOpen} onNavigate={close} />
    </div>
  )
}
