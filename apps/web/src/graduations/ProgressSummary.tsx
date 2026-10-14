import { useTranslation } from 'react-i18next'

import { formatDate } from '@/students/format'

import type { Progress } from './types'

type MeterProps = { label: string; value: number; goal: number; unit: string }

/** Barra de progresso até a meta da regra; cheia quando a meta foi atingida. */
function Meter({ label, value, goal, unit }: MeterProps) {
  const done = value >= goal
  const max = Math.max(goal, 1)
  return (
    <div className={done ? 'meter done' : 'meter'}>
      <div className="meter-head">
        <span>{label}</span>
        <strong>{value}<span className="meter-goal">/{goal} {unit}</span></strong>
      </div>
      <progress max={max} value={done ? max : value} aria-label={label} />
    </div>
  )
}

/** Tempo e treinos na faixa atual medidos contra a regra em vigor. */
export function ProgressSummary({ progress }: { progress: Progress }) {
  const { t } = useTranslation()
  const { rule } = progress
  const hasRule = rule && (rule.minMonths !== null || rule.minTrainings !== null || rule.trainingsPerDegree !== null)

  return (
    <div className="grad-progress">
      <div className="grad-progress-head">
        <span className="row-sub">{t('graduation.progress.since', { date: formatDate(progress.beltSince) })}</span>
        {progress.ready && <span className="pill pill-soft">{t(`graduation.ready.${progress.ready}`)}</span>}
      </div>
      {rule?.minMonths != null && (
        <Meter label={t('graduation.progress.time')} value={progress.monthsAtBelt} goal={rule.minMonths}
          unit={t('graduation.progress.monthsUnit')} />
      )}
      {rule?.minTrainings != null && (
        <Meter label={t('graduation.progress.beltTrainings')} value={progress.trainingsSinceBelt}
          goal={rule.minTrainings} unit={t('graduation.progress.trainingsUnit')} />
      )}
      {rule?.trainingsPerDegree != null && (
        <Meter label={t('graduation.progress.nextDegree')} value={progress.trainingsSinceLast}
          goal={rule.trainingsPerDegree} unit={t('graduation.progress.trainingsUnit')} />
      )}
      {!rule && <p className="hint">{t('graduation.progress.blackBeltRuler')}</p>}
      {rule && !hasRule && <p className="hint">{t('graduation.progress.noRule')}</p>}
    </div>
  )
}
