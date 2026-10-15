import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate } from 'react-router'

import { Dialog } from '@/components/Dialog'
import { PageHeader } from '@/components/PageHeader'
import { formatInstantDate } from '@/subscription/dates'

import { GrantForm } from './GrantForm'
import { usePlatformAcademies, usePlatformAdmin } from './platformApi'
import type { AdminAcademy } from './types'

/** Painel do dono da plataforma: todas as academias, situação e liberação manual. */
export function AdminPage() {
  const { t } = useTranslation()
  const { isAdmin, known } = usePlatformAdmin()
  const { data: academies, isLoading, isError } = usePlatformAcademies(isAdmin)
  const [granting, setGranting] = useState<AdminAcademy | null>(null)

  if (known && !isAdmin) return <Navigate to="/" replace />

  return (
    <div className="page">
      <PageHeader eyebrow={t('admin.eyebrow')} title={t('admin.title')} description={t('pages.admin')} />
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('admin.loadError')}</div>}
      {academies && academies.length === 0 && <p className="empty">{t('admin.empty')}</p>}
      {academies && academies.length > 0 && (
        <div className="card list-card">
          <ul className="people-list">
            {academies.map((a) => <AcademyRow key={a.id} academy={a} onGrant={() => setGranting(a)} />)}
          </ul>
        </div>
      )}
      <Dialog open={granting !== null} onClose={() => setGranting(null)} title={t('admin.grant.title')}>
        {granting && <GrantForm academy={granting} onDone={() => setGranting(null)} onCancel={() => setGranting(null)} />}
      </Dialog>
    </div>
  )
}

function AcademyRow({ academy: a, onGrant }: { academy: AdminAcademy; onGrant: () => void }) {
  const { t } = useTranslation()
  const until = formatInstantDate(a.status === 'GRACE' || a.status === 'SUSPENDED' ? a.suspendsAt : a.accessUntil)
  return (
    <li className="list-row admin-row">
      <span className="person-main">
        <span className="person-name">
          <span className="row-title">{a.name}</span>
          <span className="pill access-pill" data-status={a.status}>{t(`subscription.status.${a.status}`)}</span>
          {a.subscribed && <span className="pill pill-outline">{t('admin.subscribed')}</span>}
        </span>
        <span className="row-sub">
          {[a.ownerName, a.ownerEmail].filter(Boolean).join(' · ') || t('admin.noOwner')}
        </span>
        <span className="row-sub">
          {t('admin.students', { count: a.activeStudents })} · {t(`admin.until.${a.status}`, { date: until })}
        </span>
      </span>
      <span className="row-actions">
        <button type="button" className="btn btn-sm" onClick={onGrant}>{t('admin.grant.open')}</button>
      </span>
    </li>
  )
}
