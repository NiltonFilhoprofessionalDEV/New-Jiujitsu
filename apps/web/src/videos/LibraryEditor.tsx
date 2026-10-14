import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { Dialog } from '@/components/Dialog'
import { apiErrorMessage } from '@/lib/apiErrors'

import { PlaylistForm } from './PlaylistForm'
import type { Playlist, Video } from './types'
import { VideoForm } from './VideoForm'
import { useDeletePlaylist, useDeleteVideo } from './videosApi'

/** {@code undefined} dentro do objeto = criar novo. */
export type Editing = { video: Video | undefined } | { playlist: Playlist | undefined } | null
export type Deleting = { video: Video } | { playlist: Playlist } | null

type Props = {
  videos: Video[]
  editing: Editing
  deleting: Deleting
  onDone: () => void
}

/** Painéis do gestor: cadastrar/editar vídeo ou playlist e confirmar exclusões. */
export function LibraryEditor({ videos, editing, deleting, onDone }: Props) {
  const { t } = useTranslation()
  const deleteVideo = useDeleteVideo()
  const deletePlaylist = useDeletePlaylist()
  const remove = deleting && 'video' in deleting ? deleteVideo : deletePlaylist

  const editTitle = () => {
    if (!editing) return ''
    if ('video' in editing) return editing.video ? t('videos.editVideo') : t('videos.addVideo')
    return editing.playlist ? t('videos.editPlaylist') : t('videos.newPlaylist')
  }

  function close() {
    deleteVideo.reset()
    deletePlaylist.reset()
    onDone()
  }

  function confirmDelete() {
    if (!deleting) return
    if ('video' in deleting) deleteVideo.mutate(deleting.video.id, { onSuccess: close })
    else deletePlaylist.mutate(deleting.playlist.id, { onSuccess: close })
  }

  return (
    <>
      <Dialog open={editing !== null} onClose={onDone} title={editTitle()} wide>
        {editing && 'video' in editing && (
          <VideoForm key={editing.video?.id ?? 'new'} video={editing.video} onSaved={onDone} onCancel={onDone} />
        )}
        {editing && 'playlist' in editing && (
          <PlaylistForm key={editing.playlist?.id ?? 'new'} playlist={editing.playlist} videos={videos}
            onSaved={onDone} onCancel={onDone} />
        )}
      </Dialog>
      <ConfirmDialog open={deleting !== null} onClose={close} onConfirm={confirmDelete} pending={remove.isPending}
        error={remove.isError ? apiErrorMessage(t, remove.error) : null} confirmLabel={t('common.delete')}
        title={deleting && 'video' in deleting ? t('videos.deleteVideoTitle') : t('videos.deletePlaylistTitle')}
        body={deleting && 'video' in deleting
          ? t('videos.deleteVideoBody', { title: deleting.video.title })
          : t('videos.deletePlaylistBody', { title: deleting?.playlist.title ?? '' })} />
    </>
  )
}
