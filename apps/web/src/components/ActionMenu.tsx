import { MoreHorizontal, type LucideIcon } from 'lucide-react'
import { useEffect, useId, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'

export type MenuAction = { label: string; icon: LucideIcon; onSelect: () => void }

/** Ações secundárias da tela atrás de um botão "⋯"; fecha com Esc, clique fora ou ao escolher. */
export function ActionMenu({ actions }: { actions: MenuAction[] }) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const menuId = useId()

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
        <ul id={menuId} className="action-menu-list">
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
