import { createBrowserRouter, Navigate } from 'react-router'

import { StudentAccountPage } from '@/account/StudentAccountPage'
import { LoginPage } from '@/auth/pages/LoginPage'
import { RegisterAcademyPage } from '@/auth/pages/RegisterAcademyPage'
import { RedirectIfAuthenticated, RequireAuth, RequireManager, RequireStaff } from '@/auth/RequireAuth'
import { STUDENT_HOME } from '@/auth/types'
import { InvoicesPage } from '@/billing/InvoicesPage'
import { PlansPage } from '@/billing/PlansPage'
import { SubscriptionsPage } from '@/billing/SubscriptionsPage'
import { AttendancePage } from '@/classes/AttendancePage'
import { ClassesPage } from '@/classes/ClassesPage'
import { BeltsPage } from '@/graduations/BeltsPage'
import { AppShell } from '@/layout/AppShell'
import { MANAGER_NAV } from '@/layout/navItems'
import { ComingSoonPage } from '@/pages/ComingSoonPage'
import { HomePage } from '@/pages/HomePage'
import { AcceptInvitePage } from '@/public/AcceptInvitePage'
import { EnrollPage } from '@/public/EnrollPage'
import { RequestsPage } from '@/requests/RequestsPage'
import { StudentsPage } from '@/students/StudentsPage'
import { AcceptStaffInvitePage } from '@/team/AcceptStaffInvitePage'
import { TeamPage } from '@/team/TeamPage'

const IMPLEMENTED = new Set(['/', '/alunos', '/aulas', '/equipe', '/requests', '/mensalidades', '/financeiro/alunos',
  '/financeiro/faturas'])

const pendingItems = MANAGER_NAV.flatMap((g) => g.items).filter((item) => !IMPLEMENTED.has(item.to))
const pendingRoute = (item: (typeof pendingItems)[number]) => ({
  path: item.to.slice(1),
  element: <ComingSoonPage titleKey={item.labelKey} />,
})

export const router = createBrowserRouter([
  { path: '/matricula/:token', element: <EnrollPage /> },
  { path: '/convite/:token', element: <AcceptInvitePage /> },
  { path: '/equipe/convite/:token', element: <AcceptStaffInvitePage /> },
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
      { path: STUDENT_HOME, element: <StudentAccountPage /> },
      {
        element: <RequireStaff />,
        children: [
          {
            element: <AppShell />,
            children: [
              { index: true, element: <HomePage /> },
              { path: 'alunos', element: <StudentsPage /> },
              { path: 'aulas', element: <ClassesPage /> },
              { path: 'aulas/presenca', element: <AttendancePage /> },
              { path: 'aulas/faixas', element: <BeltsPage /> },
              { path: 'requests', element: <RequestsPage /> },
              ...pendingItems.filter((item) => !item.managersOnly).map(pendingRoute),
              {
                element: <RequireManager />,
                children: [
                  { path: 'equipe', element: <TeamPage /> },
                  { path: 'mensalidades', element: <PlansPage /> },
                  { path: 'financeiro/alunos', element: <SubscriptionsPage /> },
                  { path: 'financeiro/faturas', element: <InvoicesPage /> },
                  ...pendingItems.filter((item) => item.managersOnly).map(pendingRoute),
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])
