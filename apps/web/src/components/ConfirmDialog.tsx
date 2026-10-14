import { useTranslation } from 'react-i18next'

import { Dialog } from './Dialog'

type ConfirmDialogProps = {
  open: boolean
  title: string
  body: string
  confirmLabel: string
  pending?: boolean
  error?: string | null
  onConfirm: () => void
  onClose: () => void
}

export function ConfirmDialog({ open, title, body, confirmLabel, pending, error, onConfirm, onClose }: ConfirmDialogProps) {
  const { t } = useTranslation()
  return (
    <Dialog open={open} onClose={onClose} title={title}>
      <p className="dialog-text">{body}</p>
      {error && (
        <div className="alert alert-error" role="alert">
          {error}
        </div>
      )}
      <div className="form-actions">
        <button type="button" className="btn" onClick={onClose}>
          {t('common.cancel')}
        </button>
        <button type="button" className="btn btn-primary" onClick={onConfirm} disabled={pending}>
          {confirmLabel}
        </button>
      </div>
    </Dialog>
  )
}
