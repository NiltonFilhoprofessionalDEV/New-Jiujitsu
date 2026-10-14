import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api, refreshSession } from '@/lib/api'

export type AcademyProfile = {
  name: string
  cnpj: string | null
  email: string | null
  phone: string | null
  address: string | null
  city: string | null
  state: string | null
}

const PROFILE = ['academy-profile']

export function useAcademyProfile() {
  return useQuery({
    queryKey: PROFILE,
    queryFn: async () => (await api.get<AcademyProfile>('/academies/current/profile')).data,
  })
}

/** O nome da academia também está na sessão (topo e troca de academia): renova para refletir. */
export function useUpdateAcademyProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (body: AcademyProfile) =>
      (await api.put<AcademyProfile>('/academies/current/profile', body)).data,
    onSuccess: async (profile) => {
      queryClient.setQueryData(PROFILE, profile)
      await refreshSession()
    },
  })
}
