import { EnrollmentLinkCard } from './EnrollmentLinkCard'
import { InviteList } from './InviteList'
import { NewInviteForm } from './NewInviteForm'

/** Conteúdo do modal "Convites" da tela de alunos. */
export function InvitesPanel() {
  return (
    <div className="stack">
      <EnrollmentLinkCard />
      <NewInviteForm />
      <InviteList />
    </div>
  )
}
