import { ChevronLeft, X } from 'lucide-react'
import { useEffect, useId, useRef, useState, type MouseEvent, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

import { useMediaQuery } from '@/lib/useMediaQuery'

type DialogProps = {
  open: boolean
  onClose: () => void
  title: string
  children: ReactNode
  wide?: boolean
}

/** Mesma duração da animação {@code sheet-out} em components.css. */
const EXIT_MS = 200
const REDUCED_MOTION = '(prefers-reduced-motion: reduce)'

type SheetView = { title: string; children: ReactNode }

/**
 * Mantém o {@code <dialog>} aberto durante a animação de saída. Título e conteúdo da última
 * renderização aberta continuam na tela até sair: o pai costuma trocá-los no mesmo render em que fecha.
 */
function useSheetPresence(open: boolean, view: SheetView) {
  const ref = useRef<HTMLDialogElement>(null)
  const reducedMotion = useMediaQuery(REDUCED_MOTION)
  const [kept, setKept] = useState(view)
  const [wasOpen, setWasOpen] = useState(open)
  const [closing, setClosing] = useState(false)

  if (open && (kept.title !== view.title || kept.children !== view.children)) setKept(view)
  if (open !== wasOpen) {
    setWasOpen(open)
    setClosing(!open && !reducedMotion)
  }

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open) {
      if (!dialog.open) dialog.showModal()
      return
    }
    if (!closing) {
      if (dialog.open) dialog.close()
      return
    }
    const timer = window.setTimeout(() => setClosing(false), EXIT_MS)
    return () => window.clearTimeout(timer)
  }, [open, closing])

  return { ref, closing, shown: open ? view : kept }
}

function isOutside(event: MouseEvent<HTMLDialogElement>) {
  const box = event.currentTarget.getBoundingClientRect()
  return event.clientX < box.left || event.clientX > box.right || event.clientY < box.top || event.clientY > box.bottom
}

/**
 * Clique no fundo escurecido fecha. Exige que o clique também tenha começado fora: arrastar uma
 * seleção de dentro de um campo até o fundo não pode descartar o formulário.
 */
function useBackdropClose(onClose: () => void) {
  const pressedOutside = useRef(false)
  return {
    onMouseDown: (e: MouseEvent<HTMLDialogElement>) => {
      pressedOutside.current = e.target === e.currentTarget && isOutside(e)
    },
    onClick: (e: MouseEvent<HTMLDialogElement>) => {
      if (pressedOutside.current && e.target === e.currentTarget && isOutside(e)) onClose()
    },
  }
}

/**
 * Painel lateral (desliza da direita; tela cheia no celular) sobre o {@code <dialog>} nativo:
 * foco preso, Esc e o fundo fecham. O evento nativo {@code close} só é repassado enquanto
 * {@code open} ainda é true: ele também dispara no fechamento programático e desfaria uma troca
 * de painel feita pelo pai.
 */
export function Dialog({ open, onClose, title, children, wide = false }: DialogProps) {
  const { t } = useTranslation()
  const titleId = useId()
  const { ref, closing, shown } = useSheetPresence(open, { title, children })
  const backdrop = useBackdropClose(onClose)
  const className = ['dialog', wide && 'dialog-wide', closing && 'closing'].filter(Boolean).join(' ')

  return (
    <dialog
      ref={ref}
      className={className}
      aria-labelledby={titleId}
      {...backdrop}
      onCancel={(e) => {
        e.preventDefault()
        onClose()
      }}
      onClose={() => {
        if (open) onClose()
      }}
    >
      {(open || closing) && (
        <>
          <header className="dialog-header">
            <button type="button" className="icon-btn sheet-back" onClick={onClose} aria-label={t('common.back')}>
              <ChevronLeft size={22} aria-hidden />
            </button>
            <h2 id={titleId}>{shown.title}</h2>
            <button type="button" className="icon-btn sheet-close" onClick={onClose} aria-label={t('common.close')}>
              <X size={18} aria-hidden />
            </button>
          </header>
          <div className="dialog-body">{shown.children}</div>
        </>
      )}
    </dialog>
  )
}
