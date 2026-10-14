import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { NavLink } from 'react-router'

const TABS = [
  { to: '/aulas', labelKey: 'classes.tabs.classes', end: true },
  { to: '/aulas/presenca', labelKey: 'classes.tabs.attendance', end: false },
  { to: '/aulas/faixas', labelKey: 'classes.tabs.belts', end: false },
] as const

/** Cabeçalho comum das abas da área Aulas. */
export function ClassesHeader({ actions }: { actions?: ReactNode }) {
  const { t } = useTranslation()
  return (
    <>
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t('classes.title')}</h1>
        </div>
        {actions && <div className="page-actions">{actions}</div>}
      </div>
      <nav className="tabs" aria-label={t('classes.tabs.aria')}>
        {TABS.map((tab) => (
          <NavLink key={tab.to} to={tab.to} end={tab.end} className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
            {t(tab.labelKey)}
          </NavLink>
        ))}
      </nav>
    </>
  )
}
