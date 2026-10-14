import { X } from 'lucide-react'
import { useEffect, useId, useRef, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

type DialogProps = {
  open: boolean
  onClose: () => void
  title: string
  children: ReactNode
  wide?: boolean
}

/**
 * Modal sobre o {@code <dialog>} nativo: foco preso, Esc fecha e o conteúdo só monta quando aberto.
 * O evento nativo {@code close} só é repassado enquanto {@code open} ainda é true: ele também
 * dispara no fechamento programático e desfaria uma troca de modal feita pelo pai.
 */
export function Dialog({ open, onClose, title, children, wide = false }: DialogProps) {
  const { t } = useTranslation()
  const ref = useRef<HTMLDialogElement>(null)
  const titleId = useId()

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      className={wide ? 'dialog dialog-wide' : 'dialog'}
      aria-labelledby={titleId}
      onClose={() => {
        if (open) onClose()
      }}
    >
      {open && (
        <>
          <header className="dialog-header">
            <h2 id={titleId}>{title}</h2>
            <button type="button" className="icon-btn" onClick={onClose} aria-label={t('common.close')}>
              <X size={18} aria-hidden />
            </button>
          </header>
          <div className="dialog-body">{children}</div>
        </>
      )}
    </dialog>
  )
}
