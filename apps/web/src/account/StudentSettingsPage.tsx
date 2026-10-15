import { ChevronRight, Download, KeyRound, LogOut, Moon, type LucideIcon } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { Dialog } from '@/components/Dialog'
import { PageHeader } from '@/components/PageHeader'
import { Switch } from '@/components/ui/switch'
import { initials } from '@/lib/text'
import { cn } from '@/lib/utils'
import { InstallApp } from '@/pwa/InstallApp'
import { PasswordForm } from '@/settings/PasswordForm'
import { useTheme } from '@/theme/useTheme'

type RowProps = {
  icon: LucideIcon
  title: string
  description?: string
  /** Ação à direita (interruptor); sem ela, a linha inteira é um botão. */
  control?: ReactNode
  onClick?: () => void
  danger?: boolean
}

function SettingsRow({ icon: Icon, title, description, control, onClick, danger = false }: RowProps) {
  const content = (
    <>
      <span className={cn('grid size-10 shrink-0 place-items-center rounded-xl',
        danger ? 'bg-primary-soft text-brand' : 'bg-muted text-foreground')}>
        <Icon size={19} aria-hidden />
      </span>
      <span className="min-w-0 flex-1 text-left">
        <span className={cn('block text-[15px] font-semibold', danger && 'text-brand')}>{title}</span>
        {description && <span className="block text-[13px] text-muted-foreground">{description}</span>}
      </span>
      {control ?? (!danger && <ChevronRight size={18} aria-hidden className="text-subtle" />)}
    </>
  )
  const row = 'flex w-full items-center gap-3.5 px-4 py-3.5'
  if (control) return <div className={row}>{content}</div>
  return (
    <button type="button" onClick={onClick}
      className={cn(row, 'cursor-pointer border-0 bg-transparent text-foreground transition-colors hover:bg-accent')}>
      {content}
    </button>
  )
}

function SettingsGroup({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="grid gap-2">
      <h2 className="px-1 text-[11px] font-bold tracking-[0.16em] text-muted-foreground uppercase">{title}</h2>
      <div className="divide-y divide-border overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
        {children}
      </div>
    </section>
  )
}

/** Ajustes da conta do aluno: perfil, senha, aparência e sair. */
export function StudentSettingsPage() {
  const { t } = useTranslation()
  const { session, activeMembership, logout } = useAuth()
  const { theme, toggle } = useTheme()
  const [changingPassword, setChangingPassword] = useState(false)
  if (!session || !activeMembership) return null

  return (
    <div className="page page-narrow grid gap-6">
      <PageHeader eyebrow={activeMembership.academy.name} title={t('account.settings.title')} />
      <div className="flex items-center gap-4 rounded-2xl border border-border bg-card p-4 shadow-sm">
        <span aria-hidden className="grid size-14 shrink-0 place-items-center rounded-full bg-ink font-heading text-2xl tracking-wide text-ink-foreground ring-2 ring-pulse ring-offset-2 ring-offset-card">
          {initials(session.user.name)}
        </span>
        <div className="min-w-0">
          <div className="truncate text-lg font-semibold">{session.user.name}</div>
          <div className="truncate text-sm text-muted-foreground">{session.user.email}</div>
        </div>
      </div>
      <SettingsGroup title={t('account.settings.security')}>
        <SettingsRow icon={KeyRound} title={t('settings.password.title')}
          description={t('account.settings.passwordHint')} onClick={() => setChangingPassword(true)} />
      </SettingsGroup>
      <SettingsGroup title={t('account.settings.appearance')}>
        <SettingsRow icon={Moon} title={t('account.settings.darkMode')} description={t('account.settings.darkModeHint')}
          control={<Switch checked={theme === 'dark'} onCheckedChange={toggle} aria-label={t('account.settings.darkMode')} />} />
      </SettingsGroup>
      <InstallApp>
        {(install) => (
          <SettingsGroup title={t('account.settings.app')}>
            <SettingsRow icon={Download} title={t('install.menu')} description={t('install.ctaHint')} onClick={install} />
          </SettingsGroup>
        )}
      </InstallApp>
      <SettingsGroup title={t('account.settings.session')}>
        <SettingsRow icon={LogOut} title={t('topbar.logout')} danger onClick={() => void logout()} />
      </SettingsGroup>
      <Dialog open={changingPassword} onClose={() => setChangingPassword(false)} title={t('settings.password.title')}>
        <PasswordForm embedded />
      </Dialog>
    </div>
  )
}
