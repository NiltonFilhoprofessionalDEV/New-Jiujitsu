import { LogOut } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { NavLink } from 'react-router'

import { useAuth, useIsManager } from '@/auth/useAuth'
import { usePendingRequestCount } from '@/invites/invitesApi'
import { initials } from '@/lib/text'
import { ThemeToggle } from '@/theme/ThemeToggle'

import { BrandLogo } from './BrandLogo'
import type { NavGroup, NavItem } from './navItems'

const MAX_BADGE = 99

export function Sidebar({ groups, onNavigate }: { groups: NavGroup[]; onNavigate: () => void }) {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const isManager = useIsManager()
  const { data: pendingRequests = 0 } = usePendingRequestCount(isManager)
  const badgeFor = (item: NavItem) => (item.badge === 'pendingRequests' ? pendingRequests : 0)

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <BrandLogo stacked />
        <span className="sidebar-caption" title={activeMembership?.academy.name}>
          {activeMembership?.academy.name}
        </span>
      </div>
      <nav className="sidebar-nav" aria-label={t('nav.aria')}>
        {groups.map((group) => (
          <div className="nav-group" key={group.titleKey}>
            <div className="nav-group-title">{t(group.titleKey)}</div>
            {group.items.map((item) => (
              <SidebarLink key={item.to} item={item} badge={badgeFor(item)} onNavigate={onNavigate} />
            ))}
          </div>
        ))}
      </nav>
      <SidebarFooter />
    </aside>
  )
}

type LinkProps = { item: NavItem; badge: number; onNavigate: () => void }

function SidebarLink({ item: { to, labelKey, icon: Icon, end }, badge, onNavigate }: LinkProps) {
  const { t } = useTranslation()
  const label = t(labelKey)
  return (
    <NavLink to={to} end={end} className="nav-link" onClick={onNavigate}
      aria-label={badge > 0 ? t('nav.withPending', { label, count: badge }) : undefined}>
      <Icon size={19} aria-hidden />
      <span>{label}</span>
      {badge > 0 && <span className="nav-badge" aria-hidden>{badge > MAX_BADGE ? `${MAX_BADGE}+` : badge}</span>}
    </NavLink>
  )
}

function SidebarFooter() {
  const { t } = useTranslation()
  const { session, activeMembership, logout } = useAuth()
  const name = session?.user.name ?? ''
  return (
    <div className="sidebar-footer">
      <div className="account-chip">
        <span className="avatar" aria-hidden>{initials(name)}</span>
        <span className="account-text">
          <span className="account-name" title={name}>{name}</span>
          {activeMembership && <span className="account-role">{t(`roles.${activeMembership.role}`)}</span>}
        </span>
      </div>
      <div className="side-actions">
        <ThemeToggle className="side-btn icon-only" />
        <button type="button" className="side-btn" onClick={() => void logout()}>
          <LogOut size={16} aria-hidden />
          {t('topbar.logout')}
        </button>
      </div>
    </div>
  )
}
