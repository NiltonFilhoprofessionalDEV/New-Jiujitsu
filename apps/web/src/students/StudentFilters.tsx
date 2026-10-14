import { useTranslation } from 'react-i18next'

import type { AgeFilter, StatusFilter } from './types'
import { activeFilterCount, AGE_FILTERS, STATUS_FILTERS } from './useStudentQueryParams'

type Props = {
  status: StatusFilter
  age: AgeFilter
  onChange: (changes: { status?: StatusFilter; age?: AgeFilter }) => void
  onDone: () => void
}

export function StudentFilters({ status, age, onChange, onDone }: Props) {
  const { t } = useTranslation()

  return (
    <div className="stack">
      <fieldset className="filter-group">
        <legend>{t('students.filter.statusAria')}</legend>
        <div className="choice-chips">
          {STATUS_FILTERS.map((s) => (
            <button key={s} type="button" aria-pressed={status === s}
              className={status === s ? 'chip active' : 'chip'} onClick={() => onChange({ status: s })}>
              {t(`students.filter.statusOption.${s}`)}
            </button>
          ))}
        </div>
      </fieldset>
      <fieldset className="filter-group">
        <legend>{t('students.filter.ageAria')}</legend>
        <div className="choice-chips">
          {AGE_FILTERS.map((a) => (
            <button key={a} type="button" aria-pressed={age === a}
              className={age === a ? 'chip active' : 'chip'} onClick={() => onChange({ age: a })}>
              {t(`students.filter.ageOption.${a}`)}
            </button>
          ))}
        </div>
      </fieldset>
      <div className="form-actions">
        <button type="button" className="btn" disabled={activeFilterCount(status, age) === 0}
          onClick={() => onChange({ status: 'active', age: 'all' })}>
          {t('students.filter.clear')}
        </button>
        <button type="button" className="btn btn-primary" onClick={onDone}>{t('students.filter.apply')}</button>
      </div>
    </div>
  )
}
