import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate, useNavigate } from 'react-router'

import { useAuth } from '@/auth/useAuth'
import { Dialog } from '@/components/Dialog'
import { apiErrorMessage } from '@/lib/apiErrors'
import { PageHeader } from '@/components/PageHeader'
import { formatInstantDate } from '@/subscription/dates'

import { AcademyInvitePanel } from './AcademyInvitePanel'
import { DeleteAcademyDialog } from './DeleteAcademyDialog'
import { GrantForm } from './GrantForm'
import { usePlatformAcademies, usePlatformAdmin } from './platformApi'
import type { AdminAcademy } from './types'

/** Painel do dono da plataforma: todas as academias, situação e liberação manual. */
export function AdminPage() {
  const { t } = useTranslation()
  const { isAdmin, known } = usePlatformAdmin()
  const { session, switchAcademy, logout } = useAuth()
  const { data: academies, isLoading, isError } = usePlatformAcademies(isAdmin)
  const [granting, setGranting] = useState<AdminAcademy | null>(null)
  const [deleting, setDeleting] = useState<AdminAcademy | null>(null)

  async function onDeleted(academyId: string) {
    setDeleting(null)
    const active = session?.memberships.find((m) => m.id === session.activeMembershipId)
    if (active?.academy.id !== academyId) return
    const home = session?.memberships.find((m) => m.academy.id !== academyId && !m.platformSupport)
    if (home) await switchAcademy(home.id)
    else await logout()
  }

  if (known && !isAdmin) return <Navigate to="/" replace />

  return (
    <div className="page">
      <PageHeader eyebrow={t('admin.eyebrow')} title={t('admin.title')} description={t('pages.admin')} />
      {isAdmin && <AcademyInvitePanel />}
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('admin.loadError')}</div>}
      {academies && academies.length === 0 && <p className="empty">{t('admin.empty')}</p>}
      {academies && academies.length > 0 && (
        <div className="card list-card">
          <ul className="people-list">
            {academies.map((a) => (
              <AcademyRow key={a.id} academy={a} onGrant={() => setGranting(a)} onDelete={() => setDeleting(a)} />
            ))}
          </ul>
        </div>
      )}
      <Dialog open={granting !== null} onClose={() => setGranting(null)} title={t('admin.grant.title')}>
        {granting && <GrantForm academy={granting} onDone={() => setGranting(null)} onCancel={() => setGranting(null)} />}
      </Dialog>
      <Dialog open={deleting !== null} onClose={() => setDeleting(null)} title={t('admin.delete.title')}>
        {deleting && (
          <DeleteAcademyDialog
            academy={deleting}
            onDone={() => void onDeleted(deleting.id)}
            onCancel={() => setDeleting(null)}
          />
        )}
      </Dialog>
    </div>
  )
}

function AcademyRow({ academy: a, onGrant, onDelete }: {
  academy: AdminAcademy
  onGrant: () => void
  onDelete: () => void
}) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { enterAcademy } = useAuth()
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onEnter() {
    setPending(true)
    setError(null)
    try {
      await enterAcademy(a.id)
      navigate('/')
    } catch (err) {
      setError(apiErrorMessage(t, err))
      setPending(false)
    }
  }

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
        {error && <span className="field-error">{error}</span>}
        <button type="button" className="btn btn-sm btn-primary" onClick={() => void onEnter()} disabled={pending}>
          {t('admin.enter')}
        </button>
        <button type="button" className="btn btn-sm" onClick={onGrant}>{t('admin.grant.open')}</button>
        <button type="button" className="btn btn-sm btn-danger-ghost" onClick={onDelete}>{t('admin.delete.open')}</button>
      </span>
    </li>
  )
}
