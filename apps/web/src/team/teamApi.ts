import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'

import type { CreateStaffInviteInput, PublicStaffInviteInfo, StaffInvite, StaffRole, TeamMember } from './types'

const TEAM = 'team'

export function staffInviteUrl(token: string): string {
  return `${window.location.origin}/equipe/convite/${token}`
}

export function useTeam() {
  return useQuery({
    queryKey: [TEAM, 'members'],
    queryFn: async () => (await api.get<TeamMember[]>('/team')).data,
  })
}

export function useStaffInvites() {
  return useQuery({
    queryKey: [TEAM, 'invites'],
    queryFn: async () => (await api.get<StaffInvite[]>('/team/invites')).data,
  })
}

function useTeamMutation<TVars, TResult>(fn: (vars: TVars) => Promise<TResult>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [TEAM] }),
  })
}

export function useCreateStaffInvite() {
  return useTeamMutation(async (body: CreateStaffInviteInput) =>
    (await api.post<StaffInvite>('/team/invites', body)).data,
  )
}

export function useRevokeStaffInvite() {
  return useTeamMutation(async (id: string) => {
    await api.delete(`/team/invites/${id}`)
  })
}

export function useChangeRole() {
  return useTeamMutation(async ({ membershipId, role }: { membershipId: string; role: StaffRole }) =>
    (await api.put<TeamMember>(`/team/${membershipId}/role`, { role })).data,
  )
}

export function useRemoveMember() {
  return useTeamMutation(async (membershipId: string) => {
    await api.delete(`/team/${membershipId}`)
  })
}

export function useStaffInviteInfo(token: string) {
  return useQuery({
    queryKey: ['public', 'staff-invite', token],
    queryFn: async () =>
      (await api.get<PublicStaffInviteInfo>(`/public/staff-invites/${encodeURIComponent(token)}`)).data,
    retry: false,
    refetchOnWindowFocus: false,
  })
}
