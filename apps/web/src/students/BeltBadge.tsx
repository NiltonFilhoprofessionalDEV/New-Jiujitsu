import { useTranslation } from 'react-i18next'

import { BeltIcon } from './BeltIcon'
import { beltInfo } from './belts'

type Props = { belt: string | null; degrees: number; size?: number }

/** Sempre no mesmo formato: ícone, nome da faixa e, embaixo, o grau ("Sem grau" quando zero). */
export function BeltBadge({ belt, degrees, size }: Props) {
  const { t } = useTranslation()
  const info = beltInfo(belt)
  if (!info) return <span className="empty">—</span>

  const label = t(`belts.${info.slug}`)
  const degreeText = degrees > 0 ? t('students.degreeShort', { count: degrees }) : t('students.noDegree')
  return (
    <span className="belt-badge" title={`${label} · ${degreeText}`}>
      <BeltIcon belt={info.slug} degrees={degrees} size={size} />
      <span className="belt-label">
        <span className="belt-name">{label}</span>
        <span className="belt-degrees">{degreeText}</span>
      </span>
    </span>
  )
}
