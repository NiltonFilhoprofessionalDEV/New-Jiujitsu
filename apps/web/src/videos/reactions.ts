import type { Reaction } from './types'

/** Mesma ordem e conjunto do enum {@code Reaction} da API. */
export const REACTIONS: readonly { id: Reaction; emoji: string }[] = [
  { id: 'LIKE', emoji: '👍' },
  { id: 'FIRE', emoji: '🔥' },
  { id: 'CLAP', emoji: '👏' },
  { id: 'STRONG', emoji: '💪' },
  { id: 'LAUGH', emoji: '😂' },
]

/** Atalhos para inserir no comentário (o campo aceita qualquer emoji do teclado). */
export const COMMENT_EMOJIS = ['🔥', '👏', '💪', '🥋', '👊', '😂', '🙏', '❤️'] as const

export function totalReactions(reactions: Partial<Record<Reaction, number>>): number {
  return Object.values(reactions).reduce((sum, n) => sum + (n ?? 0), 0)
}
