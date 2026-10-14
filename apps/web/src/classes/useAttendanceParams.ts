import { useCallback } from 'react'
import { useSearchParams } from 'react-router'

import { parseIsoDate, todayIso } from './dates'

/** Dia e aula da chamada ficam na URL: o professor pode recarregar ou compartilhar a tela. */
export function useAttendanceParams() {
  const [params, setParams] = useSearchParams()
  const raw = params.get('data')
  const date = parseIsoDate(raw) ? (raw as string) : todayIso()
  const classId = params.get('aula')

  const selectDate = useCallback(
    (next: string) => setParams({ data: next }, { replace: true }),
    [setParams],
  )

  const selectClass = useCallback(
    (id: string) =>
      setParams((current) => {
        const next = new URLSearchParams(current)
        next.set('aula', id)
        return next
      }, { replace: true }),
    [setParams],
  )

  const closeClass = useCallback(
    () =>
      setParams((current) => {
        const next = new URLSearchParams(current)
        next.delete('aula')
        return next
      }, { replace: true }),
    [setParams],
  )

  return { date, classId, selectDate, selectClass, closeClass }
}
