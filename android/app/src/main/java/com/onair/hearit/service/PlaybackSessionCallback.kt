package com.onair.hearit.service

import android.os.Bundle
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.di.ServiceProvider
import com.onair.hearit.domain.model.PlaybackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PlaybackSessionCallback(
    private val player: Player,
    private val serviceScope: CoroutineScope,
) : MediaSession.Callback {
    private val mediaItemHelper = PlaybackMediaItemManager()

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult {
        val base = super.onConnect(session, controller)
        val sessionCommands =
            base.availableSessionCommands
                .buildUpon()
                .add(PRELOAD_RECENT_COMMAND)
                .build()
        val playerCommands = base.availablePlayerCommands

        return MediaSession.ConnectionResult
            .AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(playerCommands)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        command: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        if (command.customAction != CMD_PRELOAD_RECENT) {
            return super.onCustomCommand(session, controller, command, args)
        }
        return CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    try {
                        loadRecentInfo()?.let { info ->
                            prepareIfNeeded(info)
                        }
                        completer.set(SessionResult(SessionResult.RESULT_SUCCESS))
                    } catch (_: Exception) {
                        completer.set(SessionResult(SessionError.ERROR_UNKNOWN))
                    }
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "preload_recent_command"
        }
    }

    override fun onPlaybackResumption(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    try {
                        val info = loadRecentInfo()
                        val result =
                            info?.let { mediaItemHelper.toItemsWithStart(it) }
                                ?: EMPTY_MEDIA_ITEMS_WITH_START
                        completer.set(result)
                    } catch (_: Exception) {
                        completer.set(
                            EMPTY_MEDIA_ITEMS_WITH_START,
                        )
                    }
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "onPlaybackResumption"
        }

    private suspend fun loadRecentInfo(): PlaybackInfo? =
        withContext(Dispatchers.IO) {
            ServiceProvider
                .recentHearitRepository()
                .getRecentHearit()
                .getOrNull()
                ?.let { recent ->
                    ServiceProvider.getPlaybackInfoUseCase(recent.id).getOrNull()
                }
        }

    private fun prepareIfNeeded(info: PlaybackInfo) {
        val item = mediaItemHelper.buildMediaItem(info)
        val sameItem = player.currentMediaItem?.mediaId == item.mediaId
        val preparedOrBuffering =
            player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING
        if (!(sameItem && preparedOrBuffering)) {
            player.setMediaItems(listOf(item), 0, info.lastPosition)
            player.prepare()
        }
    }

    companion object {
        private const val CMD_PRELOAD_RECENT = "hearit.PRELOAD_RECENT"
        private val EMPTY_MEDIA_ITEMS_WITH_START =
            MediaSession.MediaItemsWithStartPosition(
                emptyList(),
                0,
                0L,
            )
        val PRELOAD_RECENT_COMMAND: SessionCommand =
            SessionCommand(CMD_PRELOAD_RECENT, Bundle.EMPTY)
    }
}
