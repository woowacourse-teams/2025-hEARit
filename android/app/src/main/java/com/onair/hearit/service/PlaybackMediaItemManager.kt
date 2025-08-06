package com.onair.hearit.service

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.PlaybackInfo

@UnstableApi
class PlaybackMediaItemManager {
    // info 재생에 필요한 오디오 정보와 메타데이터를 담고 있는 PlaybackInfo 객체를 가지고 플레이어에서 실행하기 위한 MediaItem을 구성함
    fun buildMediaItem(info: PlaybackInfo): MediaItem =
        MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(info.title)
                    .build(),
            ).build()

    /** PlaybackInfo를 기반으로 미디어 아이템 리스트와 시작 위치 정보를 포함하는 객체를 생성함
     * 이 메서드는 앱 재시작 시 마지막 재생 위치에서 이어 재생하기 위해 필요함
     * build 후에 시작 위치까지 정해주기 위함
     */
    fun toItemsWithStart(info: PlaybackInfo): MediaSession.MediaItemsWithStartPosition =
        MediaSession.MediaItemsWithStartPosition(
            listOf(buildMediaItem(info)),
            0,
            info.lastPosition,
        )
}
