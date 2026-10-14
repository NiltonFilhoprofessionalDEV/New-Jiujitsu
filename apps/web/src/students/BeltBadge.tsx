import { useTranslation } from 'react-i18next'

import { BeltIcon } from './BeltIcon'
import { beltInfo } from './belts'

type Props = { belt: string | null; degrees: number; size?: number }

export function BeltBadge({ belt, degrees, size }: Props) {
  const { t } = useTranslation()
  const info = beltInfo(belt)
  if (!info) return <span className="empty">—</span>

  const label = t(`belts.${info.slug}`)
  return (
    <span className="belt-badge" title={label}>
      <BeltIcon belt={info.slug} degrees={degrees} size={size} />
      <span className="belt-label">
        {label}
        {degrees > 0 && <span className="belt-degrees"> · {t('students.degreeShort', { count: degrees })}</span>}
      </span>
    </span>
  )
}
