import { useState } from 'react'
import { Outlet } from 'react-router'

import { useIsManager } from '@/auth/useAuth'

import { navFor } from './navItems'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

export function AppShell() {
  const isManager = useIsManager()
  const [menuOpen, setMenuOpen] = useState(false)
  const closeOnMobile = () => {
    if (window.matchMedia('(max-width: 760px)').matches) setMenuOpen(false)
  }

  return (
    <div className="shell" data-menu-open={menuOpen}>
      <Topbar onToggleMenu={() => setMenuOpen((open) => !open)} />
      <Sidebar groups={navFor(isManager)} onNavigate={closeOnMobile} />
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
