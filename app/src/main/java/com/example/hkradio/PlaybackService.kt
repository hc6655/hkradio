package com.example.hkradio

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService: MediaSessionService(), MediaSession.Callback {
    private var mediaSession: MediaSession? = null

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer
            .Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val myPlayer =
            object: ForwardingPlayer(player) {
                override fun getAvailableCommands(): Player.Commands {
                    return super.getAvailableCommands()
                        .buildUpon()
                        .remove(COMMAND_SEEK_TO_PREVIOUS)
                        .remove(COMMAND_SEEK_TO_NEXT)
                        .remove(COMMAND_SEEK_TO_DEFAULT_POSITION)
                        .build()
                }

                override fun getDuration(): Long {
                    return C.TIME_UNSET
                }

                override fun play() {
                    RadioPlayer.isPause.value = false
                    currentMediaItem?.let { setMediaItem(it) }
                    prepare()
                    super.play()
                }

                override fun pause() {
                    super.pause()
                    RadioPlayer.isPause.value = true
                }
            }


        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val extras = Bundle().apply {
            putBoolean(MediaConstants.EXTRAS_KEY_SLOT_RESERVATION_SEEK_TO_PREV, false)
            putBoolean(MediaConstants.EXTRAS_KEY_SLOT_RESERVATION_SEEK_TO_NEXT, false)
        }

        mediaSession = MediaSession.Builder(this, myPlayer)
            .setCallback(this)
            .setSessionActivity(pendingIntent)
            .setExtras(extras)
            .build()
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        val updatedMediaItems = mediaItems.map {
            it.buildUpon().setUri(it.mediaId).build()
        }.toMutableList()
        return Futures.immediateFuture(updatedMediaItems)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}