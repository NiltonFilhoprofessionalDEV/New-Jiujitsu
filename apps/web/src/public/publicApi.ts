import { useMutation, useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'

import type { PublicInviteInfo, PublicLinkInfo, SignupInput } from './types'

/** Cada leitura conta como um acesso ao link: não refaz a consulta ao focar a janela. */
const ONCE = { retry: false, staleTime: Infinity, refetchOnWindowFocus: false } as const

export function useEnrollLinkInfo(token: string) {
  return useQuery({
    queryKey: ['public', 'enroll', token],
    queryFn: async () => (await api.get<PublicLinkInfo>(`/public/enroll/${encodeURIComponent(token)}`)).data,
    ...ONCE,
  })
}

export function useInviteInfo(token: string) {
  return useQuery({
    queryKey: ['public', 'invite', token],
    queryFn: async () => (await api.get<PublicInviteInfo>(`/public/invites/${encodeURIComponent(token)}`)).data,
    ...ONCE,
  })
}

export function useEnroll(token: string) {
  return useMutation({
    mutationFn: async (input: SignupInput) => {
      await api.post(`/public/enroll/${encodeURIComponent(token)}`, input)
    },
  })
}
