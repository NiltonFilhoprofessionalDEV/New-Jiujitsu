import '@/styles/tokens.css'
import '@/styles/base.css'
import '@/styles/components.css'
import '@/styles/layout.css'
import '@/styles/students.css'
import '@/styles/invites.css'
import '@/styles/classes.css'
import '@/styles/graduations.css'
import '@/styles/billing.css'
import '@/lib/i18n'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'

import { router } from '@/app/router'
import { AuthProvider } from '@/auth/AuthProvider'

const queryClient = new QueryClient({
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
