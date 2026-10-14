import { Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { Dialog } from '@/components/Dialog'
import { apiErrorMessage } from '@/lib/apiErrors'
import { BeltBadge } from '@/students/BeltBadge'
import { formatDate } from '@/students/format'
import type { StudentDetail } from '@/students/types'

import { GraduationForm } from './GraduationForm'
import { useRemoveGraduation, useStudentGraduations } from './graduationsApi'
import { ProgressSummary } from './ProgressSummary'
import type { Graduation } from './types'

type Props = { student: StudentDetail; canEdit: boolean }

function HistoryItem({ item, onRemove }: { item: Graduation; onRemove?: () => void }) {
  const { t } = useTranslation()
  return (
    <li>
      <div className="grad-item-main">
        <BeltBadge belt={item.belt} degrees={item.degrees} />
        <span className="row-sub">
          {t(`graduation.kind.${item.kind}`)} · {formatDate(item.graduatedOn)}
          {item.recordedBy && ` · ${t('graduation.recordedBy', { name: item.recordedBy })}`}
        </span>
        {item.note && <span className="row-sub">{item.note}</span>}
      </div>
      {item.exception && <span className="pill pill-outline">{t('graduation.exceptionBadge')}</span>}
      {onRemove && (
        <button type="button" className="icon-btn" onClick={onRemove} aria-label={t('graduation.remove')}
          title={t('graduation.remove')}>
          <Trash2 size={16} aria-hidden />
        </button>
      )}
    </li>
  )
}

/** Progresso e histórico de graduações na ficha; registrar e remover são exclusivos dos gestores. */
export function GraduationSection({ student, canEdit }: Props) {
  const { t } = useTranslation()
  const { data, isLoading, isError } = useStudentGraduations(student.id)
  const remove = useRemoveGraduation(student.id)
  const [registering, setRegistering] = useState(false)
  const [removing, setRemoving] = useState<Graduation | null>(null)
  const closeRemove = () => {
    setRemoving(null)
    remove.reset()
  }

  async function onRemove(item: Graduation) {
    await remove.mutateAsync(item.id)
    closeRemove()
  }

  return (
    <section className="grad-section" aria-label={t('graduation.title')}>
      <div className="roster-head">
        <span className="section-title">{t('graduation.title')}</span>
        {canEdit && (
          <button type="button" className="btn btn-sm" onClick={() => setRegistering(true)}>
            {t('graduation.register')}
          </button>
        )}
      </div>
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('graduation.loadError')}</div>}
      {data?.progress && <ProgressSummary progress={data.progress} />}
      {!data?.progress && data?.history.length === 0 && <p className="empty">{t('graduation.empty')}</p>}
      {data && data.history.length > 0 && <h3 className="fact-title">{t('graduation.history')}</h3>}
      {data && (
        <ul className="member-list grad-history">
          {data.history.map((item) => (
            <HistoryItem key={item.id} item={item}
              onRemove={canEdit && item.removable ? () => setRemoving(item) : undefined} />
          ))}
        </ul>
      )}

      <Dialog open={registering} onClose={() => setRegistering(false)} wide
        title={t('graduation.registerFor', { name: student.name })}>
        <GraduationForm student={student} suggestedKind={data?.progress?.ready ?? null}
          onSaved={() => setRegistering(false)} onCancel={() => setRegistering(false)} />
      </Dialog>
      <ConfirmDialog
        open={removing !== null}
        title={t('graduation.remove')}
        body={t('graduation.removeConfirm', { name: student.name })}
        confirmLabel={t('common.delete')}
        pending={remove.isPending}
        error={remove.error ? apiErrorMessage(t, remove.error) : null}
        onConfirm={() => removing && void onRemove(removing).catch(() => undefined)}
        onClose={closeRemove}
      />
    </section>
  )
}
