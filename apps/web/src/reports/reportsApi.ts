import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'

import { addDays, todayIso } from '@/classes/dates'
import { api } from '@/lib/api'

import type { Period, Report } from './types'

/** Atalhos de período, em dias até hoje. O padrão é igual ao da API. */
export const PRESET_DAYS = [7, 30, 90, 365] as const
const DEFAULT_DAYS = 30

export function lastDays(days: number): Period {
  const to = todayIso()
  return { from: addDays(to, 1 - days), to }
}

export function usePeriod() {
  return useState<Period>(() => lastDays(DEFAULT_DAYS))
}

export function useReport(period: Period) {
  return useQuery({
    queryKey: ['reports', period.from, period.to],
    queryFn: async () => (await api.get<Report>('/reports', { params: period })).data,
    placeholderData: keepPreviousData,
  })
}
