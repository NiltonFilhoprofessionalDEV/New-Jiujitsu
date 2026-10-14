import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { STUDENTS } from '@/students/studentsApi'

import type { BeltRule, GraduationRequest, ReadyStudent, StudentGraduations } from './types'

export const GRADUATIONS = 'graduations'

export function useStudentGraduations(studentId: string) {
  return useQuery({
    queryKey: [GRADUATIONS, 'student', studentId],
    queryFn: async () => (await api.get<StudentGraduations>(`/students/${studentId}/graduations`)).data,
  })
}

export function useReadyStudents() {
  return useQuery({
    queryKey: [GRADUATIONS, 'ready'],
    queryFn: async () => (await api.get<ReadyStudent[]>('/graduations/ready')).data,
  })
}

export function useBeltRules() {
  return useQuery({
    queryKey: [GRADUATIONS, 'rules'],
    queryFn: async () => (await api.get<BeltRule[]>('/graduations/rules')).data,
  })
}

/** A graduação muda a faixa do aluno (lista, ficha, chamada) e a lista de prontos: invalida as duas áreas. */
function useGraduationMutation<TVars>(studentId: string, fn: (vars: TVars) => Promise<StudentGraduations>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: async (result) => {
      queryClient.setQueryData([GRADUATIONS, 'student', studentId], result)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: [GRADUATIONS, 'ready'] }),
        queryClient.invalidateQueries({ queryKey: [STUDENTS] }),
      ])
    },
  })
}

export function useRegisterGraduation(studentId: string) {
  return useGraduationMutation(studentId, async (body: GraduationRequest) =>
    (await api.post<StudentGraduations>(`/students/${studentId}/graduations`, body)).data,
  )
}

export function useRemoveGraduation(studentId: string) {
  return useGraduationMutation(studentId, async (graduationId: string) =>
    (await api.delete<StudentGraduations>(`/students/${studentId}/graduations/${graduationId}`)).data,
  )
}

/** Regras mudam quem está pronto e o progresso de cada ficha: invalida toda a área. */
export function useSaveBeltRules() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (rules: Omit<BeltRule, 'custom'>[]) =>
      (await api.put<BeltRule[]>('/graduations/rules', { rules })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [GRADUATIONS] }),
  })
}
