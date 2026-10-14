import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { initials } from '@/lib/text'
import { BeltBadge } from '@/students/BeltBadge'

import { useReadyStudents } from './graduationsApi'

/** Alunos ativos que já cumprem a regra da faixa atual; cada linha abre a ficha para registrar. */
export function ReadyList() {
  const { t } = useTranslation()
  const { data: ready, isLoading, isError } = useReadyStudents()

  if (isLoading) return <p className="empty">{t('common.loading')}</p>
  if (isError || !ready) return <div className="alert alert-error">{t('graduation.readyList.loadError')}</div>
  if (ready.length === 0) return <div className="card empty-detail">{t('graduation.readyList.empty')}</div>

  return (
    <div className="card list-card">
      <ul className="people-list">
        {ready.map((s) => (
          <li key={s.studentId}>
            <Link className="person-row" to={`/alunos?id=${s.studentId}`}>
              <span className="avatar" aria-hidden>{initials(s.name)}</span>
              <span className="person-main">
                <span className="person-name">
                  <span className="row-title">{s.name}</span>
                  {s.nickname && <span className="person-nick">“{s.nickname}”</span>}
                </span>
                <span className="row-sub">
                  {t('graduation.months', { count: s.months })} · {t('graduation.trainings', { count: s.trainings })}
                </span>
              </span>
              <span className="person-belt ready-side">
                <BeltBadge belt={s.belt} degrees={s.degrees} />
                <span className="pill pill-soft">{t(`graduation.readyList.kind.${s.kind}`)}</span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
