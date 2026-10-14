import { Moon, Sun } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { useTheme } from './useTheme'

export function ThemeToggle({ className }: { className: string }) {
  const { t } = useTranslation()
  const { theme, toggle } = useTheme()
  const label = theme === 'dark' ? t('topbar.themeLight') : t('topbar.themeDark')
  return (
    <button type="button" className={className} onClick={toggle} aria-label={label} title={label}>
      {theme === 'dark' ? <Sun size={17} aria-hidden /> : <Moon size={17} aria-hidden />}
    </button>
  )
}
