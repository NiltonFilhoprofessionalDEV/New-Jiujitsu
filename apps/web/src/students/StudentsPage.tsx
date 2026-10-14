import { Link2, Plus, SlidersHorizontal, UsersRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { MANAGER_ROLES } from '@/auth/types'
import { ActionMenu } from '@/components/ActionMenu'
import { Dialog } from '@/components/Dialog'
import { PageHeader } from '@/components/PageHeader'
import { SearchField } from '@/components/SearchField'
import { InvitesPanel } from '@/invites/InvitesPanel'
import { useDebouncedValue } from '@/lib/useDebouncedValue'
import { COMPACT_LAYOUT, useMediaQuery } from '@/lib/useMediaQuery'

import { HouseholdPanel } from './HouseholdPanel'
import { NewHouseholdForm } from './NewHouseholdForm'
import { StudentDetailPanel } from './StudentDetailPanel'
import { StudentFilters } from './StudentFilters'
import { StudentForm } from './StudentForm'
import { StudentList } from './StudentList'
import { useStudentCounts, useStudentsPage } from './studentsApi'
import type { KindFilter, StudentDetail } from './types'
import { activeFilterCount, useStudentQueryParams } from './useStudentQueryParams'

type Modal =
  | { type: 'newStudent' }
  | { type: 'editStudent'; student: StudentDetail }
  | { type: 'newFamily' }
  | { type: 'family'; id: string }
  | { type: 'filters' }
  | null

const KINDS: readonly KindFilter[] = ['all', 'training', 'responsible']

export function StudentsPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const canEdit = Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
  const params = useStudentQueryParams()
  const { update } = params
  const [search, setSearch] = useState(params.q)
  const debouncedSearch = useDebouncedValue(search)
  const [modal, setModal] = useState<Modal>(null)
  const compact = useMediaQuery(COMPACT_LAYOUT)

  useEffect(() => {
    if (debouncedSearch !== params.q) update({ q: debouncedSearch })
  }, [debouncedSearch, params.q, update])

  const filters = { q: params.q, status: params.status, age: params.age }
  const { data: page, isLoading, isError } = useStudentsPage({ ...filters, kind: params.kind, page: params.page })
  const { data: counts } = useStudentCounts(filters)
  const select = (id: string | null) => update({ id }, false)
  const closeModal = () => setModal(null)
  const filterCount = activeFilterCount(params.status, params.age)
  const detail = params.selectedId && (
    <StudentDetailPanel key={params.selectedId} studentId={params.selectedId} canEdit={canEdit}
      onEdit={(student) => setModal({ type: 'editStudent', student })}
      onOpenHousehold={(id) => setModal({ type: 'family', id })} onDeleted={() => select(null)} />
  )

  const actions = canEdit && (
    <>
      <ActionMenu actions={[
        { label: t('students.invite'), icon: Link2, onSelect: () => update({ convites: '1' }, false) },
        { label: t('students.newFamily'), icon: UsersRound, onSelect: () => setModal({ type: 'newFamily' }) },
      ]} />
      <button type="button" className="btn btn-primary" onClick={() => setModal({ type: 'newStudent' })}>
        <Plus size={18} aria-hidden /> {t('students.newStudent')}
      </button>
    </>
  )

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('students.title')}
        description={t('pages.students')} actions={actions} />

      <div className="toolbar">
        <SearchField value={search} onChange={setSearch} label={t('students.searchAria')}
          placeholder={t('students.searchPlaceholder')} />
        <button type="button" className={filterCount > 0 ? 'btn btn-outline-primary' : 'btn'}
          onClick={() => setModal({ type: 'filters' })}>
          <SlidersHorizontal size={16} aria-hidden />
          {filterCount > 0 ? t('students.filter.buttonCount', { count: filterCount }) : t('students.filter.button')}
        </button>
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
          {page && page.totalElements === 0 && <div className="card empty-detail">{t('students.empty')}</div>}
          {page && page.totalElements > 0 && (
            <StudentList page={page} selectedId={params.selectedId} onSelect={select}
              onPage={(p) => update({ page: p }, false)} />
          )}
        </div>
        {!compact && <div>{detail || <div className="card empty-detail">{t('students.emptyDetail')}</div>}</div>}
      </div>
      {compact && (
        <Dialog open={Boolean(params.selectedId)} onClose={() => select(null)} title={t('students.detail.title')} wide>
          {detail}
        </Dialog>
      )}

      <Dialog open={modal?.type === 'filters'} onClose={closeModal} title={t('students.filter.title')}>
        <StudentFilters status={params.status} age={params.age} onChange={update} onDone={closeModal} />
      </Dialog>
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
        title={t('students.invite')} wide>
        {canEdit && params.invitesOpen && <InvitesPanel />}
      </Dialog>
    </div>
  )
}
