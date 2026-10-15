import '@fontsource/bebas-neue'
import '@fontsource-variable/dm-sans'
import '@/styles/index.css'
import '@/lib/i18n'

import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'

import { router } from '@/app/router'
import { AuthProvider } from '@/auth/AuthProvider'
import { problemOf } from '@/lib/api'
import { listenForInstallPrompt } from '@/pwa/installPrompt'
import { ACADEMY_ACCESS_KEY, SUSPENDED_CODE } from '@/subscription/accessApi'

listenForInstallPrompt()

/** A academia foi suspensa no meio do uso: recarrega a situação para o shell mostrar o bloqueio. */
function onApiError(error: unknown) {
  if (problemOf(error).code === SUSPENDED_CODE) void queryClient.invalidateQueries({ queryKey: ACADEMY_ACCESS_KEY })
}

const queryClient: QueryClient = new QueryClient({
  queryCache: new QueryCache({ onError: onApiError }),
  mutationCache: new MutationCache({ onError: onApiError }),
  defaultOptions: { queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false } },
})

const root = document.getElementById('root')
if (!root) throw new Error('Elemento #root não encontrado')

createRoot(root).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)
