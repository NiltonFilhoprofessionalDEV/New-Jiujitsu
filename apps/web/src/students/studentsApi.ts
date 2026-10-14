import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { BILLING } from '@/billing/billingApi'
import { api } from '@/lib/api'

import type {
  HouseholdDetail,
  HouseholdSummary,
  Page,
  StudentCounts,
  StudentDetail,
  StudentListItem,
  StudentQuery,
  StudentRequest,
} from './types'

export const STUDENTS = 'students'
const HOUSEHOLDS = 'households'

export function useStudentsPage(query: StudentQuery) {
  return useQuery({
    queryKey: [STUDENTS, 'page', query],
    queryFn: async () => (await api.get<Page<StudentListItem>>('/students/page', { params: query })).data,
    placeholderData: keepPreviousData,
  })
}

export function useStudentCounts(query: Pick<StudentQuery, 'q' | 'status' | 'age'>) {
  return useQuery({
    queryKey: [STUDENTS, 'counts', query],
    queryFn: async () => (await api.get<StudentCounts>('/students/counts', { params: query })).data,
    placeholderData: keepPreviousData,
  })
}

export function useStudent(id: string | null) {
  return useQuery({
    queryKey: [STUDENTS, 'detail', id],
    queryFn: async () => (await api.get<StudentDetail>(`/students/${id}`)).data,
    enabled: Boolean(id),
  })
}

export function useHouseholds() {
  return useQuery({
    queryKey: [HOUSEHOLDS],
    queryFn: async () => (await api.get<HouseholdSummary[]>('/households')).data,
  })
}

export function useHousehold(id: string | null) {
  return useQuery({
    queryKey: [HOUSEHOLDS, id],
    queryFn: async () => (await api.get<HouseholdDetail>(`/households/${id}`)).data,
    enabled: Boolean(id),
  })
}

/**
 * Alunos e famílias se afetam (nome da família na lista, membros na família). O financeiro também: aluno novo
 * entra no plano padrão e nome e situação aparecem nas mensalidades e faturas.
 */
function useInvalidatingMutation<TVars, TResult>(fn: (vars: TVars) => Promise<TResult>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: [STUDENTS] }),
        queryClient.invalidateQueries({ queryKey: [HOUSEHOLDS] }),
        queryClient.invalidateQueries({ queryKey: [BILLING] }),
      ])
    },
  })
}

export function useSaveStudent() {
  return useInvalidatingMutation(async ({ id, body }: { id?: string; body: StudentRequest }) =>
    id
      ? (await api.put<StudentDetail>(`/students/${id}`, body)).data
      : (await api.post<StudentDetail>('/students', body)).data,
  )
}

export function useSetStudentActive() {
  return useInvalidatingMutation(async ({ id, active }: { id: string; active: boolean }) =>
    (await api.post<StudentDetail>(`/students/${id}/${active ? 'reactivate' : 'deactivate'}`)).data,
  )
}

export function useDeleteStudent() {
  return useInvalidatingMutation(async (id: string) => {
    await api.delete(`/students/${id}`)
  })
}

export function useCreateHousehold() {
  return useInvalidatingMutation(
    async (body: { name: string; responsibleStudentId: string | null; memberIds: string[] }) =>
      (await api.post<HouseholdDetail>('/households', body)).data,
  )
}

type HouseholdAction =
  | { type: 'rename'; name: string }
  | { type: 'responsible'; studentId: string }
  | { type: 'addMember'; studentId: string }
  | { type: 'removeMember'; studentId: string }

export function useHouseholdAction(id: string) {
  return useInvalidatingMutation(async (action: HouseholdAction) => {
    const base = `/households/${id}`
    switch (action.type) {
      case 'rename':
        return (await api.put<HouseholdDetail>(base, { name: action.name })).data
      case 'responsible':
        return (await api.put<HouseholdDetail>(`${base}/responsible`, { studentId: action.studentId })).data
      case 'addMember':
        return (await api.post<HouseholdDetail>(`${base}/members`, { studentId: action.studentId })).data
      case 'removeMember':
        return (await api.delete<HouseholdDetail>(`${base}/members/${action.studentId}`)).data
    }
  })
}

export function useAddDependent(householdId: string) {
  return useInvalidatingMutation(async (body: StudentRequest) =>
    (await api.post<StudentDetail>(`/households/${householdId}/dependents`, body)).data,
  )
}

export function useDeleteHousehold() {
  return useInvalidatingMutation(async (id: string) => {
    await api.delete(`/households/${id}`)
  })
}
