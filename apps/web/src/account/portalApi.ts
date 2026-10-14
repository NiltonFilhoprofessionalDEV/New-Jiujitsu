import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'

import type { Overview, PortalInvoice, Profile, TodayClass } from './types'

const PORTAL = 'portal'
/** A janela de check-in abre e fecha com o relógio: reconsulta as aulas de hoje periodicamente. */
const TODAY_REFRESH_MS = 60_000

const studentPath = (studentId: string) => `/portal/students/${encodeURIComponent(studentId)}`

export function useProfiles() {
  return useQuery({
    queryKey: [PORTAL, 'profiles'],
    queryFn: async () => (await api.get<Profile[]>('/portal/profiles')).data,
  })
}

export function useOverview(studentId: string) {
  return useQuery({
    queryKey: [PORTAL, studentId, 'overview'],
    queryFn: async () => (await api.get<Overview>(studentPath(studentId))).data,
  })
}

export function usePortalInvoices(studentId: string) {
  return useQuery({
    queryKey: [PORTAL, studentId, 'invoices'],
    queryFn: async () => (await api.get<PortalInvoice[]>(`${studentPath(studentId)}/invoices`)).data,
  })
}

export function useTodayClasses(studentId: string) {
  return useQuery({
    queryKey: [PORTAL, studentId, 'today'],
    queryFn: async () => (await api.get<TodayClass[]>(`${studentPath(studentId)}/today`)).data,
    refetchInterval: TODAY_REFRESH_MS,
  })
}

/** Marca ou desfaz o check-in; a resposta já traz as aulas de hoje e a presença entra no histórico. */
export function useCheckIn(studentId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ classId, undo }: { classId: string; undo: boolean }) => {
      const url = `${studentPath(studentId)}/check-ins/${encodeURIComponent(classId)}`
      return (await (undo ? api.delete<TodayClass[]>(url) : api.post<TodayClass[]>(url))).data
    },
    onSuccess: (today) => {
      queryClient.setQueryData([PORTAL, studentId, 'today'], today)
      return queryClient.invalidateQueries({ queryKey: [PORTAL, studentId, 'overview'] })
    },
  })
}
