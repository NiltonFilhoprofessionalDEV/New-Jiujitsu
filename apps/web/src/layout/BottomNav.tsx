import { Menu } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { matchPath, NavLink, useLocation } from 'react-router'

import { cn } from '@/lib/utils'

import type { NavItem } from './navItems'

type Props = {
  items: NavItem[]
  /** Com "Mais": abre o menu completo (equipe). */
  onMore?: () => void
  moreOpen?: boolean
  /** Fecha o menu em tela cheia do celular, que deixa esta barra à vista. */
  onNavigate?: () => void
}

const tabClass = (active: boolean) => cn(
  'relative flex flex-1 flex-col items-center justify-center gap-1 pt-1 text-[11px] font-semibold no-underline transition-colors hover:no-underline',
  'before:absolute before:top-0 before:h-0.5 before:w-8 before:rounded-full before:bg-pulse before:shadow-glow before:transition-opacity',
  active ? 'text-ink-foreground before:opacity-100' : 'text-ink-muted before:opacity-0',
)

/** Barra de abas fixa no rodapé, só no celular: os destinos principais ao alcance do polegar. */
export function BottomNav({ items, onMore, moreOpen = false, onNavigate }: Props) {
  const { t } = useTranslation()
  const { pathname } = useLocation()
  const onTab = items.some((item) => matchPath({ path: item.to, end: item.end ?? false }, pathname))

  return (
    <nav aria-label={t('nav.tabsAria')}
      className="fixed inset-x-0 bottom-0 z-20 flex h-[calc(64px+env(safe-area-inset-bottom))] border-t border-ink-border bg-ink/95 pb-[env(safe-area-inset-bottom)] backdrop-blur-md min-[861px]:hidden">
      {items.map(({ to, labelKey, icon: Icon, end }) => (
        <NavLink key={to} to={to} end={end} onClick={onNavigate}
          className={({ isActive }) => tabClass(isActive && !moreOpen)}>
          {({ isActive }) => (
            <>
              <Icon size={22} aria-hidden className={isActive && !moreOpen ? 'text-pulse' : undefined} />
              {t(labelKey)}
            </>
          )}
        </NavLink>
      ))}
      {onMore && (
        <button type="button" onClick={onMore} aria-expanded={moreOpen}
          className={cn(tabClass(moreOpen || !onTab), 'cursor-pointer border-0 bg-transparent')}>
          <Menu size={22} aria-hidden className={moreOpen || !onTab ? 'text-pulse' : undefined} />
          {t('nav.more')}
        </button>
      )}
    </nav>
  )
}
