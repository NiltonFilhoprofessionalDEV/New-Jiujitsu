import { LogOut, Menu } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { ThemeToggle } from '@/theme/ThemeToggle'

import { BrandLogo } from './BrandLogo'

/**
 * Barra superior compacta. Com {@code onToggleMenu} (equipe, só no celular) abre o menu lateral, que
 * já traz tema e sair; sem ele (área do aluno) a própria barra oferece essas ações.
 */
export function Topbar({ onToggleMenu }: { onToggleMenu?: () => void }) {
  const { t } = useTranslation()
  const { activeMembership, logout } = useAuth()

  return (
    <header className="topbar">
      {onToggleMenu && (
        <button type="button" className="icon-btn" onClick={onToggleMenu} aria-label={t('topbar.menu')}>
          <Menu size={20} aria-hidden />
        </button>
      )}
      <BrandLogo />
      <span className="topbar-title" title={activeMembership?.academy.name}>
        {activeMembership?.academy.name}
      </span>
      {!onToggleMenu && (
        <>
          <ThemeToggle className="icon-btn" />
          <button type="button" className="icon-btn" onClick={() => void logout()}
            aria-label={t('topbar.logout')} title={t('topbar.logout')}>
            <LogOut size={18} aria-hidden />
          </button>
        </>
      )}
    </header>
  )
}
