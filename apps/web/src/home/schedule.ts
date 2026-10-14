import type { DayClass } from '@/classes/types'

/** Aula sem horário de término conta como "no tatame" por este tempo após o início. */
export const DEFAULT_CLASS_MINUTES = 60

const MINUTES_PER_HOUR = 60

/** {@code 19:30} ou {@code 19:30:00} → minutos desde a meia-noite. */
export function minutesOf(time: string): number {
  const [hours, minutes] = time.split(':')
  return Number(hours) * MINUTES_PER_HOUR + Number(minutes)
}

export function minutesNow(now: Date): number {
  return now.getHours() * MINUTES_PER_HOUR + now.getMinutes()
}

export type TodayPlan = { live: DayClass[]; next: DayClass | null }

/** Aulas acontecendo agora e a próxima a começar no dia. */
export function planToday(classes: DayClass[], nowMinutes: number): TodayPlan {
  const sorted = [...classes].sort((a, b) => minutesOf(a.startTime) - minutesOf(b.startTime))
  const live = sorted.filter((c) => {
    const start = minutesOf(c.startTime)
    const end = c.endTime ? minutesOf(c.endTime) : start + DEFAULT_CLASS_MINUTES
    return start <= nowMinutes && nowMinutes < end
  })
  const next = sorted.find((c) => minutesOf(c.startTime) > nowMinutes) ?? null
  return { live, next }
}

/** Link da chamada já aberta no dia e na aula (mesmos parâmetros de {@code useAttendanceParams}). */
export function attendanceHref(date: string, classId: string): string {
  return `/aulas/presenca?${new URLSearchParams({ data: date, aula: classId })}`
}
