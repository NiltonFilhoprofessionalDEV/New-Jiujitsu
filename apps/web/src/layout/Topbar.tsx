import type { ReactNode } from 'react'

import { useAuth } from '@/auth/useAuth'

import { BrandLogo } from './BrandLogo'

/**
 * Barra superior compacta: no celular para a equipe (o menu abre pelo "Mais" da barra inferior) e sempre
 * na área do aluno, onde {@code children} traz as abas do computador.
 */
export function Topbar({ children }: { children?: ReactNode }) {
  const { activeMembership } = useAuth()

  return (
    <header className="topbar">
      <BrandLogo />
      <span className="topbar-title" title={activeMembership?.academy.name}>
        {activeMembership?.academy.name}
      </span>
      {children}
    </header>
  )
}
