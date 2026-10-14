import { Plus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { MANAGER_ROLES } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { Dialog } from '@/components/Dialog'
import { PageHeader } from '@/components/PageHeader'
import { apiErrorMessage } from '@/lib/apiErrors'

import { ClassesTable } from './ClassesTable'
import { ClassForm } from './ClassForm'
import { useArchiveClass, useClasses } from './classesApi'
import { ClassStudentsPanel } from './ClassStudentsPanel'
import type { TrainingClass } from './types'

type Modal =
  | { type: 'new' }
  | { type: 'edit'; trainingClass: TrainingClass }
  | { type: 'students'; trainingClass: TrainingClass }
  | { type: 'archive'; trainingClass: TrainingClass }
  | null

export function ClassesPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const canEdit = Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
  const { data: classes, isLoading, isError } = useClasses()
  const archive = useArchiveClass()
  const [modal, setModal] = useState<Modal>(null)
  const [created, setCreated] = useState(false)
  const close = () => {
    setModal(null)
    archive.reset()
  }

  async function onArchive(id: string) {
    await archive.mutateAsync(id)
    close()
  }

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('classes.tabs.classes')} description={t('pages.classes')}
        actions={canEdit && (
          <button type="button" className="btn btn-primary" onClick={() => setModal({ type: 'new' })}>
            <Plus size={16} aria-hidden /> {t('classes.new')}
          </button>
        )} />

      <div className="stack">
        {created && <div className="alert alert-success" role="status">{t('classes.created')}</div>}
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('classes.loadError')}</div>}
        {classes && classes.length === 0 && <div className="card empty-detail">{t('classes.empty')}</div>}
        {classes && classes.length > 0 && (
          <ClassesTable classes={classes} canEdit={canEdit}
            onStudents={(c) => setModal({ type: 'students', trainingClass: c })}
            onEdit={(c) => setModal({ type: 'edit', trainingClass: c })}
            onArchive={(c) => setModal({ type: 'archive', trainingClass: c })} />
        )}
      </div>

      <Dialog open={modal?.type === 'new' || modal?.type === 'edit'} onClose={close} wide
        title={modal?.type === 'edit' ? t('classes.edit') : t('classes.new')}>
        <ClassForm trainingClass={modal?.type === 'edit' ? modal.trainingClass : undefined} onCancel={close}
          onSaved={() => {
            setCreated(modal?.type === 'new')
            close()
          }} />
      </Dialog>
      <Dialog open={modal?.type === 'students'} onClose={close} wide
        title={modal?.type === 'students' ? t('classes.students.title', { title: modal.trainingClass.title }) : ''}>
        {modal?.type === 'students' && <ClassStudentsPanel trainingClass={modal.trainingClass} canEdit={canEdit} />}
      </Dialog>
      <ConfirmDialog
        open={modal?.type === 'archive'}
        title={t('classes.archive')}
        body={modal?.type === 'archive' ? t('classes.archiveConfirm', { title: modal.trainingClass.title }) : ''}
        confirmLabel={t('classes.archive')}
        pending={archive.isPending}
        error={archive.error ? apiErrorMessage(t, archive.error) : null}
        onConfirm={() => modal?.type === 'archive' && void onArchive(modal.trainingClass.id).catch(() => undefined)}
        onClose={close}
      />
    </div>
  )
}
