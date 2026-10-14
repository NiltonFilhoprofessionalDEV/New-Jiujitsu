import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { MANAGER_ROLES } from '@/auth/types'
import { Dialog } from '@/components/Dialog'
import { InvitesPanel } from '@/invites/InvitesPanel'
import { useDebouncedValue } from '@/lib/useDebouncedValue'

import { HouseholdPanel } from './HouseholdPanel'
import { NewHouseholdForm } from './NewHouseholdForm'
import { StudentDetailPanel } from './StudentDetailPanel'
import { StudentForm } from './StudentForm'
import { StudentTable } from './StudentTable'
import { useStudentCounts, useStudentsPage } from './studentsApi'
import type { AgeFilter, KindFilter, StatusFilter, StudentDetail } from './types'
import { useStudentQueryParams } from './useStudentQueryParams'

type Modal =
  | { type: 'newStudent' }
  | { type: 'editStudent'; student: StudentDetail }
  | { type: 'newFamily' }
  | { type: 'family'; id: string }
  | null

const KINDS: readonly KindFilter[] = ['all', 'training', 'responsible']
const AGES: readonly AgeFilter[] = ['all', 'kids', 'adult', 'under18', 'adult18']

export function StudentsPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const canEdit = Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
  const params = useStudentQueryParams()
  const { update } = params
  const [search, setSearch] = useState(params.q)
  const debouncedSearch = useDebouncedValue(search)
  const [modal, setModal] = useState<Modal>(null)

  useEffect(() => {
    if (debouncedSearch !== params.q) update({ q: debouncedSearch })
  }, [debouncedSearch, params.q, update])

  const filters = { q: params.q, status: params.status, age: params.age }
  const { data: page, isLoading, isError } = useStudentsPage({ ...filters, kind: params.kind, page: params.page })
  const { data: counts } = useStudentCounts(filters)
  const select = (id: string | null) => update({ id }, false)
  const closeModal = () => setModal(null)

  return (
    <div className="page page-wide">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t('students.title')}</h1>
        </div>
        {canEdit && (
          <div className="page-actions">
            <button type="button" className="btn" onClick={() => update({ convites: '1' }, false)}>
              {t('topbar.invites')}
            </button>
            <button type="button" className="btn" onClick={() => setModal({ type: 'newFamily' })}>
              {t('students.newFamily')}
            </button>
            <button type="button" className="btn btn-primary" onClick={() => setModal({ type: 'newStudent' })}>
              {t('students.newStudent')}
            </button>
          </div>
        )}
      </div>

      <div className="toolbar">
        <input type="search" className="input grow" value={search} onChange={(e) => setSearch(e.target.value)}
          placeholder={t('students.searchPlaceholder')} aria-label={t('students.searchAria')} />
        <select className="input" value={params.status} aria-label={t('students.filter.statusAria')}
          onChange={(e) => update({ status: e.target.value as StatusFilter })}>
          <option value="active">{t('students.filter.active')}</option>
          <option value="inactive">{t('students.filter.inactive')}</option>
          <option value="all">{t('students.filter.statusAll')}</option>
        </select>
        <select className="input" value={params.age} aria-label={t('students.filter.ageAria')}
          onChange={(e) => update({ age: e.target.value as AgeFilter })}>
          {AGES.map((a) => (
            <option key={a} value={a}>{t(`students.filter.age.${a}`)}</option>
          ))}
        </select>
        <span className="toolbar-count">
          {t('students.countOf', { shown: page?.content.length ?? 0, total: page?.totalElements ?? 0 })}
        </span>
      </div>

      <div className="chips" role="tablist" aria-label={t('students.kind.aria')}>
        {KINDS.map((k) => (
          <button key={k} type="button" role="tab" aria-selected={params.kind === k}
            className={params.kind === k ? 'chip active' : 'chip'} onClick={() => update({ kind: k })}>
            {t(`students.kind.filter.${k}`)} <strong>{counts?.[k] ?? 0}</strong>
          </button>
        ))}
      </div>

      {counts && counts.hiddenInactive > 0 && params.status === 'active' && (
        <p className="hint">
          {t('students.hiddenInactive', { count: counts.hiddenInactive })}{' '}
          <button type="button" className="link-btn" onClick={() => update({ status: 'inactive' })}>
            {t('students.seeInactive')}
          </button>
        </p>
      )}

      <div className="split">
        <div>
          {isLoading && <p className="empty">{t('common.loading')}</p>}
          {isError && <div className="alert alert-error">{t('students.listError')}</div>}
          {page && page.totalElements === 0 && <p className="empty">{t('students.empty')}</p>}
          {page && page.totalElements > 0 && (
            <StudentTable page={page} selectedId={params.selectedId} onSelect={select}
              onPage={(p) => update({ page: p }, false)} />
          )}
        </div>
        <div>
          {params.selectedId ? (
            <StudentDetailPanel key={params.selectedId} studentId={params.selectedId} canEdit={canEdit}
              onEdit={(student) => setModal({ type: 'editStudent', student })}
              onOpenHousehold={(id) => setModal({ type: 'family', id })} onDeleted={() => select(null)} />
          ) : (
            <div className="card empty-detail">{t('students.emptyDetail')}</div>
          )}
        </div>
      </div>

      <Dialog open={modal?.type === 'newStudent' || modal?.type === 'editStudent'} onClose={closeModal} wide
        title={modal?.type === 'editStudent' ? t('students.form.edit') : t('students.newStudent')}>
        <StudentForm student={modal?.type === 'editStudent' ? modal.student : undefined} onCancel={closeModal}
          onSaved={(s) => { closeModal(); select(s.id) }} />
      </Dialog>
      <Dialog open={modal?.type === 'newFamily'} onClose={closeModal} title={t('students.newFamily')} wide>
        <NewHouseholdForm onCancel={closeModal} onCreated={(h) => setModal({ type: 'family', id: h.id })} />
      </Dialog>
      <Dialog open={modal?.type === 'family'} onClose={closeModal} title={t('households.title')} wide>
        {modal?.type === 'family' && (
          <HouseholdPanel householdId={modal.id} canEdit={canEdit} onClose={closeModal}
            onOpenStudent={(id) => { closeModal(); select(id) }} />
        )}
      </Dialog>
      <Dialog open={canEdit && params.invitesOpen} onClose={() => update({ convites: null }, false)}
        title={t('topbar.invites')} wide>
        {canEdit && params.invitesOpen && <InvitesPanel />}
      </Dialog>
    </div>
  )
}
