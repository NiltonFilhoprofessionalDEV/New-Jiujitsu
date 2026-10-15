export function enrollUrl(token: string): string {
  return `${window.location.origin}/matricula/${token}`
}

export function inviteUrl(token: string): string {
  return `${window.location.origin}/convite/${token}`
}
