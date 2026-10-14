import type { ComponentType } from 'react'
import { createBrowserRouter, Navigate } from 'react-router'

import { LoginPage } from '@/auth/pages/LoginPage'
import { RegisterAcademyPage } from '@/auth/pages/RegisterAcademyPage'
import { RedirectIfAuthenticated, RequireAuth, RequireManager, RequireStaff } from '@/auth/RequireAuth'
import { STUDENT_HOME } from '@/auth/types'
import { AppShell } from '@/layout/AppShell'

import { PageLoading, RouteError } from './RouteFallbacks'

/** Cada tela vira um chunk próprio, baixado na primeira visita. */
function page<M>(load: () => Promise<M>, pick: (module: M) => ComponentType) {
  return async () => ({ Component: pick(await load()) })
}

const managerRoutes = [
  { path: 'equipe', lazy: page(() => import('@/team/TeamPage'), (m) => m.TeamPage) },
  { path: 'mensalidades', lazy: page(() => import('@/billing/PlansPage'), (m) => m.PlansPage) },
  { path: 'financeiro/alunos', lazy: page(() => import('@/billing/SubscriptionsPage'), (m) => m.SubscriptionsPage) },
  { path: 'financeiro/faturas', lazy: page(() => import('@/billing/InvoicesPage'), (m) => m.InvoicesPage) },
]

/** Mesmas telas para equipe e aluno; os links internos são relativos à base de cada área. */
const videoRoutes = [
  { path: 'videos', lazy: page(() => import('@/videos/VideoLibraryPage'), (m) => m.VideoLibraryPage) },
  { path: 'videos/:videoId', lazy: page(() => import('@/videos/VideoPlayerPage'), (m) => m.VideoPlayerPage) },
]

const studentRoutes = [
  { index: true, lazy: page(() => import('@/account/StudentAccountPage'), (m) => m.StudentAccountPage) },
  { path: 'ajustes', lazy: page(() => import('@/account/StudentSettingsPage'), (m) => m.StudentSettingsPage) },
  ...videoRoutes,
]

const staffRoutes = [
  { index: true, lazy: page(() => import('@/home/HomePage'), (m) => m.HomePage) },
  { path: 'alunos', lazy: page(() => import('@/students/StudentsPage'), (m) => m.StudentsPage) },
  { path: 'aulas', lazy: page(() => import('@/classes/ClassesPage'), (m) => m.ClassesPage) },
  { path: 'aulas/presenca', lazy: page(() => import('@/classes/AttendancePage'), (m) => m.AttendancePage) },
  { path: 'aulas/faixas', lazy: page(() => import('@/graduations/BeltsPage'), (m) => m.BeltsPage) },
  { path: 'requests', lazy: page(() => import('@/requests/RequestsPage'), (m) => m.RequestsPage) },
  { path: 'configuracoes', lazy: page(() => import('@/settings/SettingsPage'), (m) => m.SettingsPage) },
  { path: 'relatorios', lazy: page(() => import('@/reports/ReportsPage'), (m) => m.ReportsPage) },
  ...videoRoutes,
  {
    path: 'relatorios/professores',
    lazy: page(() => import('@/reports/InstructorsReportPage'), (m) => m.InstructorsReportPage),
  },
  { element: <RequireManager />, children: managerRoutes },
]

export const router = createBrowserRouter([
  {
    hydrateFallbackElement: <PageLoading />,
    errorElement: <RouteError />,
    children: [
      { path: '/matricula/:token', lazy: page(() => import('@/public/EnrollPage'), (m) => m.EnrollPage) },
      { path: '/convite/:token', lazy: page(() => import('@/public/AcceptInvitePage'), (m) => m.AcceptInvitePage) },
      {
        path: '/equipe/convite/:token',
        lazy: page(() => import('@/team/AcceptStaffInvitePage'), (m) => m.AcceptStaffInvitePage),
      },
      {
        element: <RedirectIfAuthenticated />,
        children: [
          { path: '/login', element: <LoginPage /> },
          { path: '/register-academy', element: <RegisterAcademyPage /> },
        ],
      },
      {
        element: <RequireAuth />,
        children: [
          {
            path: STUDENT_HOME,
            lazy: page(() => import('@/account/StudentShell'), (m) => m.StudentShell),
            children: studentRoutes,
          },
          {
            element: <RequireStaff />,
            children: [{ element: <AppShell />, children: staffRoutes }],
          },
        ],
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
