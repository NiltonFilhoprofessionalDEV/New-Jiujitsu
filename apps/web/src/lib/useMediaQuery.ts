import { useCallback, useSyncExternalStore } from 'react'

/** Mesmo corte do {@code .split} em students.css: abaixo dele, lista e detalhe não cabem lado a lado. */
export const COMPACT_LAYOUT = '(max-width: 1000px)'

/** Acompanha uma media query CSS (ex.: largura de tela) e re-renderiza quando ela muda. */
export function useMediaQuery(query: string) {
  const subscribe = useCallback(
    (onChange: () => void) => {
      const media = window.matchMedia(query)
      media.addEventListener('change', onChange)
      return () => media.removeEventListener('change', onChange)
    },
    [query],
  )
  return useSyncExternalStore(subscribe, () => window.matchMedia(query).matches, () => false)
}
