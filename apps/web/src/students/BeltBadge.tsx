import { useTranslation } from 'react-i18next'

import { beltInfo } from './belts'

export function BeltBadge({ belt, degrees }: { belt: string | null; degrees: number }) {
  const { t } = useTranslation()
  const info = beltInfo(belt)
  if (!info) return <span className="empty">—</span>

  const [main, stripe] = info.colors
  const background = stripe
    ? `linear-gradient(180deg, ${main} 0 33%, ${stripe} 33% 67%, ${main} 67% 100%)`
    : main
  const label = t(`belts.${info.slug}`)

  return (
    <span className="belt-badge" title={label}>
      <span className="belt-swatch" style={{ background }} aria-hidden />
      <span>
        {label}
        {degrees > 0 && <span className="belt-degrees"> · {t('students.degreeShort', { count: degrees })}</span>}
      </span>
    </span>
  )
}
