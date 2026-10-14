import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { BeltBadge } from '@/students/BeltBadge'
import { formatDate } from '@/students/format'

import { useReadyStudents } from './graduationsApi'

/** Alunos ativos que já cumprem a regra da faixa atual; o link abre a ficha para registrar. */
export function ReadyList() {
  const { t } = useTranslation()
  const { data: ready, isLoading, isError } = useReadyStudents()

  if (isLoading) return <p className="empty">{t('common.loading')}</p>
  if (isError || !ready) return <div className="alert alert-error">{t('graduation.readyList.loadError')}</div>
  if (ready.length === 0) return <div className="card empty-detail">{t('graduation.readyList.empty')}</div>

  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th>{t('graduation.readyList.student')}</th>
            <th>{t('graduation.readyList.current')}</th>
            <th>{t('graduation.readyList.readyFor')}</th>
            <th className="hide-narrow">{t('graduation.readyList.since')}</th>
            <th>{t('graduation.readyList.progress')}</th>
          </tr>
        </thead>
        <tbody>
          {ready.map((s) => (
            <tr key={s.studentId}>
              <td>
                <Link className="row-link" to={`/alunos?id=${s.studentId}`}>
                  <span className="row-title">{s.name}</span>
                  {s.nickname && <span className="row-sub">“{s.nickname}”</span>}
                </Link>
              </td>
              <td><BeltBadge belt={s.belt} degrees={s.degrees} /></td>
              <td><span className="pill pill-soft">{t(`graduation.ready.${s.kind}`)}</span></td>
              <td className="hide-narrow nowrap">{formatDate(s.since)}</td>
              <td className="nowrap">
                {t('graduation.months', { count: s.months })} · {t('graduation.trainings', { count: s.trainings })}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
