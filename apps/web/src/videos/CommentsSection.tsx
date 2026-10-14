import { Trash2 } from 'lucide-react'
import { useRef, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { STAFF_ROLES } from '@/auth/types'
import { ConfirmDialog } from '@/components/ConfirmDialog'
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
    <li className="comment">
      <span className="avatar avatar-sm" aria-hidden>{initials(comment.authorName)}</span>
      <div className="comment-main">
        <div className="comment-head">
          <span className="comment-author">{comment.authorName}</span>
          {STAFF_ROLES.includes(comment.authorRole) && (
            <span className="pill pill-soft">{t(`roles.${comment.authorRole}`)}</span>
          )}
          <time className="row-sub" dateTime={comment.createdAt}>{formatDate(comment.createdAt)}</time>
        </div>
        <p className="comment-body">{comment.body}</p>
      </div>
      {comment.canDelete && (
        <button type="button" className="icon-btn" onClick={onDelete} aria-label={t('videos.comments.delete')}>
          <Trash2 size={16} aria-hidden />
        </button>
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
    <form className="comment-form" onSubmit={onSubmit}>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      <label htmlFor="comment-body" className="sr-only">{t('videos.comments.label')}</label>
      <textarea id="comment-body" ref={inputRef} className="input" rows={2} maxLength={MAX_COMMENT_LENGTH}
        value={body} onChange={(e) => setBody(e.target.value)} placeholder={t('videos.comments.placeholder')} />
      <div className="comment-form-actions">
        <div className="emoji-picks" role="group" aria-label={t('videos.comments.emojiAria')}>
          {COMMENT_EMOJIS.map((emoji) => (
            <button key={emoji} type="button" className="emoji-pick" onClick={() => insertEmoji(emoji)}
              aria-label={t('videos.comments.insertEmoji', { emoji })}>
              {emoji}
            </button>
          ))}
        </div>
        <button type="submit" className="btn btn-primary btn-sm" disabled={add.isPending || !body.trim()}>
          {t('videos.comments.submit')}
        </button>
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
    <section className="comments" aria-labelledby="comments-title">
      <h2 id="comments-title" className="section-title">
        {t('videos.commentsCount', { count: comments.length })}
      </h2>
      <CommentForm videoId={videoId} />
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {!isLoading && comments.length === 0 && <p className="empty">{t('videos.comments.empty')}</p>}
      <ul className="comment-list">
        {comments.map((c) => <CommentItem key={c.id} comment={c} onDelete={() => setDeleting(c)} />)}
      </ul>
      <ConfirmDialog open={deleting !== null} title={t('videos.comments.deleteTitle')}
        body={t('videos.comments.deleteBody')} confirmLabel={t('common.delete')} pending={remove.isPending}
        error={remove.isError ? apiErrorMessage(t, remove.error) : null}
        onClose={() => { remove.reset(); setDeleting(null) }}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </section>
  )
}
