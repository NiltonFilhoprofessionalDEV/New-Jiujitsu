import { useTranslation } from 'react-i18next'

import { formatDate } from '@/students/format'

import type { Progress, RuleTarget } from './types'

function RuleGoals({ rule }: { rule: RuleTarget | null }) {
  const { t } = useTranslation()
  if (!rule) return <p className="hint">{t('graduation.progress.blackBeltRuler')}</p>

  const beltGoal = [
    rule.minMonths !== null && t('graduation.months', { count: rule.minMonths }),
    rule.minTrainings !== null && t('graduation.trainings', { count: rule.minTrainings }),
  ].filter(Boolean)
  if (beltGoal.length === 0 && rule.trainingsPerDegree === null) {
    return <p className="hint">{t('graduation.progress.noRule')}</p>
  }
  return (
    <p className="hint">
      {beltGoal.length > 0 && t('graduation.progress.beltGoal', { goal: beltGoal.join(' · ') })}
      {beltGoal.length > 0 && rule.trainingsPerDegree !== null && ' · '}
      {rule.trainingsPerDegree !== null && t('graduation.progress.degreeGoal', { count: rule.trainingsPerDegree })}
    </p>
  )
}

/** Tempo e treinos na faixa atual e desde a última graduação, com a meta da regra em vigor. */
export function ProgressSummary({ progress }: { progress: Progress }) {
  const { t } = useTranslation()
  const sameStart = progress.lastSince === progress.beltSince

  return (
    <div className="grad-progress">
      {progress.ready && (
        <span className="pill pill-soft">{t(`graduation.ready.${progress.ready}`)}</span>
      )}
      <p>
        {t('graduation.progress.atBelt', {
          date: formatDate(progress.beltSince),
          months: t('graduation.months', { count: progress.monthsAtBelt }),
          trainings: t('graduation.trainings', { count: progress.trainingsSinceBelt }),
        })}
      </p>
      {!sameStart && (
        <p>
          {t('graduation.progress.sinceLast', {
            date: formatDate(progress.lastSince),
            trainings: t('graduation.trainings', { count: progress.trainingsSinceLast }),
          })}
        </p>
      )}
      <RuleGoals rule={progress.rule} />
    </div>
  )
}
