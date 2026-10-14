import { useTranslation } from 'react-i18next'
import { NavLink } from 'react-router'

import type { NavGroup } from './navItems'

export function Sidebar({ groups, onNavigate }: { groups: NavGroup[]; onNavigate: () => void }) {
  const { t } = useTranslation()

  return (
    <nav className="sidebar" aria-label="Menu principal">
      {groups.map((group) => (
        <div className="nav-group" key={group.titleKey}>
          <div className="nav-group-title">{t(group.titleKey)}</div>
          {group.items.map(({ to, labelKey, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className="nav-link"
              title={t(labelKey)}
              aria-label={t(labelKey)}
              onClick={onNavigate}
            >
              <Icon size={20} aria-hidden />
              <span className="nav-label">{t(labelKey)}</span>
            </NavLink>
          ))}
        </div>
      ))}
    </nav>
  )
}
