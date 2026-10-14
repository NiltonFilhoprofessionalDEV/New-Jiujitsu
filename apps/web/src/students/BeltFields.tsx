import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { BELTS, degreeOptions } from './belts'

type Props = { idPrefix: string; initialBelt?: string | null; initialDegrees?: number }

/** Campos {@code belt} e {@code degrees} de um formulário; os graus disponíveis dependem da faixa escolhida. */
export function BeltFields({ idPrefix, initialBelt, initialDegrees }: Props) {
  const { t } = useTranslation()
  const [belt, setBelt] = useState(initialBelt ?? 'branca')

  return (
    <>
      <div className="field">
        <label htmlFor={`${idPrefix}-belt`}>{t('students.form.belt')}</label>
        <select id={`${idPrefix}-belt`} name="belt" className="input" value={belt} onChange={(e) => setBelt(e.target.value)}>
          {(['adult', 'kids'] as const).map((group) => (
            <optgroup key={group} label={t(`belts.group.${group}`)}>
              {BELTS.filter((b) => b.group === group).map((b) => (
                <option key={b.slug} value={b.slug}>
                  {t(`belts.${b.slug}`)}
                </option>
              ))}
            </optgroup>
          ))}
        </select>
      </div>
      <div className="field">
        <label htmlFor={`${idPrefix}-degrees`}>{t('students.form.degree')}</label>
        <select key={belt} id={`${idPrefix}-degrees`} name="degrees" className="input"
          defaultValue={initialBelt === belt ? initialDegrees : undefined}>
          {degreeOptions(belt).map((d) => (
            <option key={d} value={d}>
              {t('students.degree', { count: d })}
            </option>
          ))}
        </select>
      </div>
    </>
  )
}
