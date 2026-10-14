import {
  Award,
  BarChart3,
  CalendarDays,
  ClipboardCheck,
  Receipt,
  UserPlus,
  Users,
  type LucideIcon,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

type Action = { key: string; to: string; icon: LucideIcon; tone?: 'pulse' | 'inverse'; managersOnly?: boolean }

/** A chamada é a ação mais usada no dia a dia, por isso vem em vermelho e primeiro. */
const ACTIONS: Action[] = [
  { key: 'attendance', to: '/aulas/presenca', icon: ClipboardCheck, tone: 'pulse' },
  { key: 'classes', to: '/aulas', icon: CalendarDays, tone: 'inverse' },
  { key: 'students', to: '/alunos', icon: Users },
  { key: 'graduate', to: '/aulas/faixas', icon: Award },
  { key: 'invite', to: '/alunos?convites=1', icon: UserPlus, managersOnly: true },
  { key: 'invoices', to: '/financeiro/faturas', icon: Receipt, managersOnly: true },
  { key: 'reports', to: '/relatorios', icon: BarChart3 },
]

const TILE_COUNT = 6

export function QuickActions({ isManager }: { isManager: boolean }) {
  const { t } = useTranslation()
  const actions = ACTIONS.filter((a) => isManager || !a.managersOnly).slice(0, TILE_COUNT)

  return (
    <section className="home-section" aria-labelledby="home-actions">
      <h2 id="home-actions" className="sr-only">{t('home.actions.title')}</h2>
      <div className="quick-grid">
        {actions.map(({ key, to, icon: Icon, tone }) => (
          <Link key={key} to={to} className={tone ? `quick-tile ${tone}` : 'quick-tile'}>
            <Icon size={20} aria-hidden />
            <strong>{t(`home.actions.${key}.title`)}</strong>
            <span>{t(`home.actions.${key}.hint`)}</span>
          </Link>
        ))}
      </div>
    </section>
  )
}
