import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { STUDENTS } from '@/students/studentsApi'
import type { StudentDetail } from '@/students/types'

import type { ApproveRank, CreateInviteInput, EnrollmentLink, EnrollmentRequestItem, Invite, InviteTab } from './types'

const LINK = 'enrollment-link'
const INVITES = 'invites'
const REQUESTS = 'enrollment-requests'

export function useEnrollmentLink() {
  return useQuery({
    queryKey: [LINK],
    queryFn: async () => (await api.get<EnrollmentLink>('/invites/link')).data,
  })
}

type LinkAction = { type: 'rotate' } | { type: 'configure'; enabled: boolean; expiresOn: string | null }

export function useLinkAction() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (action: LinkAction) =>
      action.type === 'rotate'
        ? (await api.post<EnrollmentLink>('/invites/link/rotate')).data
        : (await api.put<EnrollmentLink>('/invites/link', { enabled: action.enabled, expiresOn: action.expiresOn }))
            .data,
    onSuccess: (link) => queryClient.setQueryData([LINK], link),
  })
}

export function useInvites(tab: InviteTab) {
  return useQuery({
    queryKey: [INVITES, tab],
    queryFn: async () => (await api.get<Invite[]>('/invites', { params: { status: tab } })).data,
  })
}

function useInviteMutation<TVars>(fn: (vars: TVars) => Promise<Invite>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [INVITES] }),
  })
}

export function useCreateInvite() {
  return useInviteMutation(async (body: CreateInviteInput) => (await api.post<Invite>('/invites', body)).data)
}

export function useInviteAction() {
  return useInviteMutation(async ({ id, action }: { id: string; action: 'revoke' | 'renew' }) =>
    (await api.post<Invite>(`/invites/${id}/${action}`)).data,
  )
}

export function useEnrollmentRequests() {
  return useQuery({
    queryKey: [REQUESTS, 'list'],
    queryFn: async () => (await api.get<EnrollmentRequestItem[]>('/requests/enrollments')).data,
  })
}

export function usePendingRequestCount(enabled: boolean) {
  return useQuery({
    queryKey: [REQUESTS, 'count'],
    queryFn: async () => (await api.get<{ pending: number }>('/requests/count')).data.pending,
    enabled,
    staleTime: 30_000,
  })
}

/** Aprovar cria um aluno: atualiza também a lista de alunos. */
export function useDecideRequest() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, approve, rank }: { id: string; approve: boolean; rank?: ApproveRank }) => {
      if (approve) return (await api.post<StudentDetail>(`/requests/enrollments/${id}/approve`, rank)).data
      await api.post(`/requests/enrollments/${id}/reject`)
      return null
    },
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: [REQUESTS] }),
        queryClient.invalidateQueries({ queryKey: [STUDENTS] }),
      ]),
  })
}
