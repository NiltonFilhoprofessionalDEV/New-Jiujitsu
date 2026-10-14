import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { DataItem } from '@/components/DataItem'
import { useDecideRequest, useEnrollmentRequests } from '@/invites/invitesApi'
import type { EnrollmentRequestItem } from '@/invites/types'
import { apiErrorMessage } from '@/lib/apiErrors'
import { BeltBadge } from '@/students/BeltBadge'
import { formatCpf, formatDate } from '@/students/format'

type Approved = { studentId: string; name: string }

export function RequestsPage() {
  const { t } = useTranslation()
  const { data: requests, isLoading, isError } = useEnrollmentRequests()
  const decide = useDecideRequest()
  const [rejecting, setRejecting] = useState<EnrollmentRequestItem | null>(null)
  const [approved, setApproved] = useState<Approved | null>(null)

  function approve(request: EnrollmentRequestItem) {
    decide.mutate(
      { id: request.id, approve: true },
      { onSuccess: (student) => student && setApproved({ studentId: student.id, name: student.name }) },
    )
  }

  function reject() {
    if (!rejecting) return
    decide.mutate({ id: rejecting.id, approve: false }, { onSuccess: () => setRejecting(null) })
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t('requests.title')}</h1>
          <p className="hint">{t('requests.subtitle')}</p>
        </div>
      </div>

      <div className="stack">
        {approved && (
          <div className="notice" role="status">
            <p>{t('requests.approved', { name: approved.name })}</p>
            <Link className="link-btn" to={`/alunos?id=${approved.studentId}`}>{t('requests.seeStudent')}</Link>
          </div>
        )}
        {decide.error && !rejecting && (
          <div className="alert alert-error" role="alert">{apiErrorMessage(t, decide.error)}</div>
        )}
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('requests.loadError')}</div>}
        {requests?.length === 0 && <div className="card empty">{t('requests.empty')}</div>}
        <div className="request-list">
          {requests?.map((request) => (
            <RequestCard key={request.id} request={request} busy={decide.isPending}
              onApprove={() => approve(request)} onReject={() => setRejecting(request)} />
          ))}
        </div>
      </div>

      <ConfirmDialog
        open={rejecting !== null}
        title={t('requests.reject')}
        body={t('requests.rejectConfirm', { name: rejecting?.name ?? '' })}
        confirmLabel={t('requests.reject')}
        pending={decide.isPending}
        error={decide.error && rejecting ? apiErrorMessage(t, decide.error) : null}
        onConfirm={reject}
        onClose={() => { setRejecting(null); decide.reset() }}
      />
    </div>
  )
}

type CardProps = { request: EnrollmentRequestItem; busy: boolean; onApprove: () => void; onReject: () => void }

function RequestCard({ request, busy, onApprove, onReject }: CardProps) {
  const { t } = useTranslation()
  return (
    <article className="card request-card" aria-label={request.name}>
      <header className="detail-header">
        <div>
          <h2>{request.name}</h2>
          {request.nickname && <p className="empty">“{request.nickname}”</p>}
        </div>
        <span className="hint">{t('requests.sentAt', { date: formatDate(request.createdAt) })}</span>
      </header>
      <BeltBadge belt={request.belt} degrees={request.degrees} />
      <dl className="data-list">
        <DataItem label={t('students.form.email')} value={request.email} />
        <DataItem label={t('students.form.cpf')} value={formatCpf(request.cpf)} />
        <DataItem label={t('students.form.phone')} value={request.phone} />
        <DataItem label={t('students.form.birthDate')} value={formatDate(request.birthDate)} />
      </dl>
      <footer className="detail-actions">
        <button type="button" className="btn btn-primary" disabled={busy} onClick={onApprove}>
          {t('requests.approve')}
        </button>
        <button type="button" className="btn btn-danger-ghost" disabled={busy} onClick={onReject}>
          {t('requests.reject')}
        </button>
      </footer>
    </article>
  )
}
