import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { BELTS, degreeOptions } from './belts'

type Props = {
  idPrefix: string
  initialBelt?: string | null
  initialDegrees?: number
  onChange?: (belt: string, degrees: number) => void
}

function degreesFor(belt: string, current: number) {
  const options = degreeOptions(belt)
  return options.includes(current) ? current : (options[0] ?? 0)
}

/** Campos {@code belt} e {@code degrees} de um formulário; os graus disponíveis dependem da faixa escolhida. */
export function BeltFields({ idPrefix, initialBelt, initialDegrees, onChange }: Props) {
  const { t } = useTranslation()
  const [belt, setBelt] = useState(initialBelt ?? 'branca')
  const [degrees, setDegrees] = useState(initialDegrees ?? 0)

  function changeBelt(next: string) {
    const nextDegrees = degreesFor(next, degrees)
    setBelt(next)
    setDegrees(nextDegrees)
    onChange?.(next, nextDegrees)
  }

  function changeDegrees(next: number) {
    setDegrees(next)
    onChange?.(belt, next)
  }

  return (
    <>
      <div className="field">
        <label htmlFor={`${idPrefix}-belt`}>{t('students.form.belt')}</label>
        <select id={`${idPrefix}-belt`} name="belt" className="input" value={belt} onChange={(e) => changeBelt(e.target.value)}>
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
        <select id={`${idPrefix}-degrees`} name="degrees" className="input" value={degrees}
          onChange={(e) => changeDegrees(Number(e.target.value))}>
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
