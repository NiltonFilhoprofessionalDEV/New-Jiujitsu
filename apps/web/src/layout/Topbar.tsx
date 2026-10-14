import { Bell, ClipboardList, Menu, MessageCircle, Moon, Sun, UserPlus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { MANAGER_ROLES } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'
import { usePendingRequestCount } from '@/invites/invitesApi'
import { useTheme } from '@/theme/useTheme'

const REQUESTS_PATH = '/requests'

const ICON_LINKS = [
  { to: '/alunos?convites=1', labelKey: 'topbar.invites', icon: UserPlus },
  { to: REQUESTS_PATH, labelKey: 'topbar.requests', icon: ClipboardList },
  { to: '/mensagens', labelKey: 'topbar.messages', icon: MessageCircle },
  { to: '/notificacoes', labelKey: 'topbar.notifications', icon: Bell },
] as const

export function Topbar({ onToggleMenu }: { onToggleMenu: () => void }) {
  const { t } = useTranslation()
  const { activeMembership, logout } = useAuth()
  const { theme, toggle } = useTheme()
  const academyName = activeMembership?.academy.name ?? ''
  const isManager = Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
  const { data: pendingRequests = 0 } = usePendingRequestCount(isManager)
  const badgeFor = (to: string) => (to === REQUESTS_PATH ? pendingRequests : 0)

  return (
    <header className="topbar">
      <button type="button" className="icon-btn" onClick={onToggleMenu} aria-label={t('topbar.menu')}>
        <Menu size={20} aria-hidden />
      </button>
      <div className="topbar-brand">
        <span className="brand-mark" aria-hidden>
          {academyName.charAt(0).toUpperCase()}
        </span>
        <span className="brand-name" title={academyName}>
          {academyName}
        </span>
      </div>

      {ICON_LINKS.map(({ to, labelKey, icon: Icon }) => {
        const badge = badgeFor(to)
        const label = badge > 0 ? `${t(labelKey)} (${badge})` : t(labelKey)
        return (
          <Link key={to} to={to} className="icon-btn hide-mobile" aria-label={label} title={label}>
            <Icon size={19} aria-hidden />
            {badge > 0 && <span className="icon-badge" aria-hidden>{badge > 99 ? '99+' : badge}</span>}
          </Link>
        )
      })}

      <span className="pill trial-pill hide-mobile">{t('topbar.trial')}</span>
      <button
        type="button"
        className="icon-btn"
        onClick={toggle}
        aria-label={theme === 'dark' ? t('topbar.themeLight') : t('topbar.themeDark')}
        title={theme === 'dark' ? t('topbar.themeLight') : t('topbar.themeDark')}
      >
        {theme === 'dark' ? <Sun size={19} aria-hidden /> : <Moon size={19} aria-hidden />}
      </button>
      <span className="topbar-divider hide-mobile" />
      {activeMembership && <span className="pill role-pill">{t(`roles.${activeMembership.role}`).toUpperCase()}</span>}
      <button type="button" className="btn btn-logout" onClick={() => void logout()}>
        {t('topbar.logout')}
      </button>
    </header>
  )
}
