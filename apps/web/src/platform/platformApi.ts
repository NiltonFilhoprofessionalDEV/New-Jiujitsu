import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { ACADEMY_ACCESS_KEY } from '@/subscription/accessApi'

import type { AdminAcademy, GrantRequest } from './types'

const ACADEMIES = ['platform-academies']
const INVITES = ['platform-academy-invites']

export type AcademyCreationInvite = {
  id: string
  token: string
  expiresAt: string
  createdAt: string
}

export function academyInviteUrl(token: string): string {
  return `${window.location.origin}/academia/convite/${token}`
}

/**
 * Se a conta logada administra a plataforma (o e-mail vem da configuração do servidor).
 * {@code known}: a resposta já chegou; antes disso, {@code isAdmin} é false.
 */
export function usePlatformAdmin(): { isAdmin: boolean; known: boolean } {
  const { data } = useQuery({
    queryKey: ['platform-access'],
    queryFn: async () => (await api.get<{ admin: boolean }>('/platform/access')).data,
    staleTime: Infinity,
  })
  return { isAdmin: data?.admin ?? false, known: data !== undefined }
}

export function usePlatformAcademies(enabled: boolean) {
  return useQuery({
    queryKey: ACADEMIES,
    queryFn: async () => (await api.get<AdminAcademy[]>('/platform/academies')).data,
    enabled,
  })
}

export function useDeleteAcademy() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ academyId, confirmName }: { academyId: string; confirmName: string }) => {
      await api.delete(`/platform/academies/${academyId}`, { data: { confirmName } })
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ACADEMIES })
    },
  })
}

export function useGrantAccess() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ academyId, body }: { academyId: string; body: GrantRequest }) =>
      (await api.post<AdminAcademy>(`/platform/academies/${academyId}/grants`, body)).data,
    onSuccess: (updated) => {
      queryClient.setQueryData<AdminAcademy[]>(ACADEMIES, (list) =>
        list?.map((a) => (a.id === updated.id ? updated : a)))
      void queryClient.invalidateQueries({ queryKey: ACADEMY_ACCESS_KEY })
    },
  })
}

export function useAcademyInvites() {
  return useQuery({
    queryKey: INVITES,
    queryFn: async () => (await api.get<AcademyCreationInvite[]>('/platform/academy-invites')).data,
  })
}

export function useCreateAcademyInvite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => (await api.post<AcademyCreationInvite>('/platform/academy-invites')).data,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: INVITES })
    },
  })
}

export function useRevokeAcademyInvite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/platform/academy-invites/${id}`)
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: INVITES })
    },
  })
}
