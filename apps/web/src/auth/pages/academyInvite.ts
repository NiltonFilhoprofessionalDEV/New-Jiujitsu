import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'

export type AcademyInviteStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'REVOKED'

export function useAcademyInviteInfo(token: string) {
  return useQuery({
    queryKey: ['public', 'academy-invite', token],
    queryFn: async () =>
      (await api.get<{ status: AcademyInviteStatus }>(`/public/academy-invites/${encodeURIComponent(token)}`)).data,
    enabled: token.length > 0,
  })
}
