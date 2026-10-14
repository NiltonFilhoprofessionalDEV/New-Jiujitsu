import { MessageCircle, Trash2 } from 'lucide-react'
import { useRef, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { STAFF_ROLES } from '@/auth/types'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Textarea } from '@/components/ui/textarea'
import { apiErrorMessage } from '@/lib/apiErrors'
import { initials } from '@/lib/text'

import { COMMENT_EMOJIS } from './reactions'
import type { VideoComment } from './types'
import { useLocalDate } from './useLocalDate'
import { useAddComment, useComments, useDeleteComment } from './videosApi'

const MAX_COMMENT_LENGTH = 1000

const COMMENT_DATE: Intl.DateTimeFormatOptions = { dateStyle: 'short', timeStyle: 'short' }

function CommentItem({ comment, onDelete }: { comment: VideoComment; onDelete: () => void }) {
  const { t } = useTranslation()
  const formatDate = useLocalDate(COMMENT_DATE)
  return (
    <li className="flex items-start gap-3">
      <span aria-hidden className="grid size-9 shrink-0 place-items-center rounded-full bg-ink text-[13px] font-semibold text-ink-foreground">
        {initials(comment.authorName)}
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
          <span className="text-sm font-semibold">{comment.authorName}</span>
          {STAFF_ROLES.includes(comment.authorRole) && <Badge variant="soft">{t(`roles.${comment.authorRole}`)}</Badge>}
          <time className="text-xs text-muted-foreground" dateTime={comment.createdAt}>{formatDate(comment.createdAt)}</time>
        </div>
        <p className="mt-1.5 mb-0 w-fit max-w-full rounded-2xl rounded-tl-md bg-muted px-3.5 py-2.5 text-[15px] leading-relaxed whitespace-pre-line [overflow-wrap:anywhere]">
          {comment.body}
        </p>
      </div>
      {comment.canDelete && (
        <Button variant="ghost" size="icon-sm" onClick={onDelete} aria-label={t('videos.comments.delete')}
          className="text-muted-foreground hover:text-brand">
          <Trash2 size={16} aria-hidden />
        </Button>
      )}
    </li>
  )
}

function CommentForm({ videoId }: { videoId: string }) {
  const { t } = useTranslation()
  const add = useAddComment(videoId)
  const [body, setBody] = useState('')
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)

  function insertEmoji(emoji: string) {
    const input = inputRef.current
    const start = input?.selectionStart ?? body.length
    const end = input?.selectionEnd ?? body.length
    const next = (body.slice(0, start) + emoji + body.slice(end)).slice(0, MAX_COMMENT_LENGTH)
    setBody(next)
    requestAnimationFrame(() => {
      input?.focus()
      input?.setSelectionRange(start + emoji.length, start + emoji.length)
    })
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!body.trim()) return
    setError(null)
    try {
      await add.mutateAsync(body.trim())
      setBody('')
    } catch (err) {
      setError(apiErrorMessage(t, err))
    }
  }

  return (
    <form className="grid gap-2" onSubmit={onSubmit}>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      <label htmlFor="comment-body" className="sr-only">{t('videos.comments.label')}</label>
      <Textarea id="comment-body" ref={inputRef} rows={2} maxLength={MAX_COMMENT_LENGTH}
        value={body} onChange={(e) => setBody(e.target.value)} placeholder={t('videos.comments.placeholder')} />
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex flex-wrap gap-0.5" role="group" aria-label={t('videos.comments.emojiAria')}>
          {COMMENT_EMOJIS.map((emoji) => (
            <button key={emoji} type="button" onClick={() => insertEmoji(emoji)} aria-label={t('videos.comments.insertEmoji', { emoji })}
              className="grid size-9 cursor-pointer place-items-center rounded-lg border-0 bg-transparent text-xl transition-transform hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring active:scale-90">
              {emoji}
            </button>
          ))}
        </div>
        <Button type="submit" size="sm" disabled={add.isPending || !body.trim()}>{t('videos.comments.submit')}</Button>
      </div>
    </form>
  )
}

/** Comentários publicam na hora; autor e gestor podem apagar. */
export function CommentsSection({ videoId }: { videoId: string }) {
  const { t } = useTranslation()
  const { data: comments = [], isLoading } = useComments(videoId)
  const remove = useDeleteComment(videoId)
  const [deleting, setDeleting] = useState<VideoComment | null>(null)

  return (
    <Card aria-labelledby="comments-title">
      <CardHeader>
        <CardTitle id="comments-title" className="m-0 flex items-center gap-2">
          <MessageCircle size={18} aria-hidden className="text-pulse" />
          {t('videos.commentsCount', { count: comments.length })}
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-6">
        <CommentForm videoId={videoId} />
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {!isLoading && comments.length === 0 && (
          <p className="m-0 text-center text-sm text-muted-foreground">{t('videos.comments.empty')}</p>
        )}
        {comments.length > 0 && (
          <ul className="m-0 grid list-none gap-5 p-0">
            {comments.map((c) => <CommentItem key={c.id} comment={c} onDelete={() => setDeleting(c)} />)}
          </ul>
        )}
      </CardContent>
      <ConfirmDialog open={deleting !== null} title={t('videos.comments.deleteTitle')}
        body={t('videos.comments.deleteBody')} confirmLabel={t('common.delete')} pending={remove.isPending}
        error={remove.isError ? apiErrorMessage(t, remove.error) : null}
        onClose={() => { remove.reset(); setDeleting(null) }}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </Card>
  )
}
