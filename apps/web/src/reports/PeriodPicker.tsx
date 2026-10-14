import { useTranslation } from 'react-i18next'

import { lastDays, PRESET_DAYS } from './reportsApi'
import type { Period } from './types'

type Props = { value: Period; onChange: (period: Period) => void }

/** Atalhos (7, 30, 90 e 365 dias) ou datas livres; datas incompletas não disparam consulta. */
export function PeriodPicker({ value, onChange }: Props) {
  const { t } = useTranslation()
  const update = (patch: Partial<Period>) => {
    const next = { ...value, ...patch }
    if (next.from && next.to) onChange(next)
  }
  return (
    <div className="toolbar">
      <div className="chips" role="group" aria-label={t('reports.presetsAria')}>
        {PRESET_DAYS.map((days) => {
          const preset = lastDays(days)
          const active = preset.from === value.from && preset.to === value.to
          return (
            <button key={days} type="button" aria-pressed={active} className={active ? 'chip active' : 'chip'}
              onClick={() => onChange(preset)}>
              {t('reports.lastDays', { count: days })}
            </button>
          )
        })}
      </div>
      <label className="inline-field">
        <span>{t('reports.from')}</span>
        <input type="date" className="input" value={value.from} max={value.to}
          onChange={(e) => update({ from: e.target.value })} />
      </label>
      <label className="inline-field">
        <span>{t('reports.to')}</span>
        <input type="date" className="input" value={value.to} min={value.from}
          onChange={(e) => update({ to: e.target.value })} />
      </label>
    </div>
  )
}
