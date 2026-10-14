import { useCallback, useSyncExternalStore } from 'react'

export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'theme'
const SYSTEM_DARK = '(prefers-color-scheme: dark)'
const THEME_COLOR: Record<Theme, string> = { light: '#f4f4f5', dark: '#070707' }
const listeners = new Set<() => void>()

/** Só o escuro marca o {@code <html>}; o estado inicial vem de {@code public/theme-init.js}. */
function read(): Theme {
  return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
}

function storedChoice(): Theme | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    return value === 'light' || value === 'dark' ? value : null
  } catch {
    return null
  }
}

function apply(theme: Theme) {
  if (theme === 'dark') document.documentElement.dataset.theme = 'dark'
  else delete document.documentElement.dataset.theme
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', THEME_COLOR[theme])
  listeners.forEach((l) => l())
}

/** Sem escolha salva, o app acompanha o modo do sistema mesmo com a tela aberta. */
function onSystemChange(event: MediaQueryListEvent) {
  if (storedChoice() === null) apply(event.matches ? 'dark' : 'light')
}

function subscribe(listener: () => void) {
  const media = window.matchMedia(SYSTEM_DARK)
  if (listeners.size === 0) media.addEventListener('change', onSystemChange)
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
    if (listeners.size === 0) media.removeEventListener('change', onSystemChange)
  }
}

export function useTheme() {
  const theme = useSyncExternalStore(subscribe, read, () => 'light' as Theme)

  const toggle = useCallback(() => {
    const next: Theme = read() === 'dark' ? 'light' : 'dark'
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // armazenamento indisponível (modo privado): o tema vale só para a sessão
    }
    apply(next)
  }, [])

  return { theme, toggle }
}
