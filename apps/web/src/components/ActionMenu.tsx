import { MoreHorizontal, type LucideIcon } from 'lucide-react'
import { useEffect, useId, useLayoutEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'

export type MenuAction = { label: string; icon: LucideIcon; onSelect: () => void }

/** Folga mínima entre a caixa do menu e a borda da tela. */
const VIEWPORT_GAP = 8

/** Ações secundárias da tela atrás de um botão "⋯"; fecha com Esc, clique fora ou ao escolher. */
export function ActionMenu({ actions }: { actions: MenuAction[] }) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const [alignStart, setAlignStart] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const listRef = useRef<HTMLUListElement>(null)
  const menuId = useId()

  // A caixa cresce para a esquerda do botão; se o botão estiver perto da borda esquerda (celular), cresce para a direita.
  useLayoutEffect(() => {
    const root = rootRef.current
    const list = listRef.current
    if (!open || !root || !list) return
    setAlignStart(root.getBoundingClientRect().right - list.offsetWidth < VIEWPORT_GAP)
  }, [open])

  useEffect(() => {
    if (!open) return
    const onPointerDown = (e: PointerEvent) => {
      if (!rootRef.current?.contains(e.target as Node)) setOpen(false)
    }
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return
      e.preventDefault()
      setOpen(false)
    }
    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  return (
    <div className="action-menu" ref={rootRef}>
      <button type="button" className="btn btn-icon" aria-label={t('common.moreActions')} aria-expanded={open}
        aria-controls={menuId} onClick={() => setOpen((v) => !v)}>
        <MoreHorizontal size={18} aria-hidden />
      </button>
      {open && (
        <ul id={menuId} ref={listRef} className={alignStart ? 'action-menu-list align-start' : 'action-menu-list'}>
          {actions.map(({ label, icon: Icon, onSelect }) => (
            <li key={label}>
              <button type="button" onClick={() => { setOpen(false); onSelect() }}>
                <Icon size={16} aria-hidden />
                {label}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
