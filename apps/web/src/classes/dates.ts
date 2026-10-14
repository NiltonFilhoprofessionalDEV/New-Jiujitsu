import type { Weekday } from './types'

export const WEEKDAYS: readonly Weekday[] = [1, 2, 3, 4, 5, 6, 7]

const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/
const DAYS_IN_WEEK = 7

/**
 * Datas de calendário como {@code yyyy-MM-dd} no fuso do navegador. Nunca use {@code toISOString()}:
 * ele converte para UTC e, à noite no Brasil, já devolve o dia seguinte.
 */
export function toIsoDate(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export function todayIso(): string {
  return toIsoDate(new Date())
}

/** Data local ao meio-dia (imune a horário de verão); {@code null} se o texto não for uma data válida. */
export function parseIsoDate(value: string | null): Date | null {
  if (!value || !ISO_DATE.test(value)) return null
  const date = new Date(Number(value.slice(0, 4)), Number(value.slice(5, 7)) - 1, dayOfMonth(value), 12)
  return toIsoDate(date) === value ? date : null
}

export function addDays(iso: string, days: number): string {
  const date = parseIsoDate(iso) ?? new Date()
  date.setDate(date.getDate() + days)
  return toIsoDate(date)
}

export function isoWeekday(iso: string): Weekday {
  const day = (parseIsoDate(iso) ?? new Date()).getDay()
  return (day === 0 ? DAYS_IN_WEEK : day) as Weekday
}

/** Os 7 dias (segunda a domingo) da semana que contém a data. */
export function weekOf(iso: string): string[] {
  const monday = addDays(iso, 1 - isoWeekday(iso))
  return WEEKDAYS.map((_, i) => addDays(monday, i))
}

export function dayOfMonth(iso: string): number {
  return Number(iso.slice(8, 10))
}

/** {@code 19:00:00} → {@code 19:00}. */
export function shortTime(time: string | null): string {
  return time ? time.slice(0, 5) : ''
}

export function timeRange(start: string, end: string | null): string {
  return end ? `${shortTime(start)}–${shortTime(end)}` : shortTime(start)
}
