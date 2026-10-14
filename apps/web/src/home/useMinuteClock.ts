import { useEffect, useState } from 'react'

const ONE_MINUTE_MS = 60_000

/** Hora atual, atualizada a cada minuto: o painel troca "agora" e "próxima aula" sozinho. */
export function useMinuteClock(): Date {
  const [now, setNow] = useState(() => new Date())
  useEffect(() => {
    const id = window.setInterval(() => setNow(new Date()), ONE_MINUTE_MS)
    return () => window.clearInterval(id)
  }, [])
  return now
}
