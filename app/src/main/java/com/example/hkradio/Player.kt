package com.example.hkradio

import android.content.ComponentName
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaSession2
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken

object Player {
    private lateinit var controller: MediaController

    fun setController(controller: MediaController) {
        this.controller = controller
    }

    fun gettingController(): MediaController? {
        return if (::controller.isInitialized) {
            controller
        } else {
            null
        }
    }

    fun play(data: ChannelData) {
        if (!this::controller.isInitialized)
            return

        val media = MediaItem.Builder()
            .setMediaId(data.link)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(data.name)
                    .setArtworkUri(Uri.parse("android.resource://com.example.hkradio/" + data.artwork))
                    .build()
            ).build()
        controller.setMediaItem(media)
        controller.prepare()
        controller.play()
    }

    fun pause() {
        if (!this::controller.isInitialized)
            return

        if (controller.isPlaying)
            controller.pause()
    }

    fun resume() {
        if (!this::controller.isInitialized)
            return

        if (!controller.isPlaying)
            controller.play()
    }

    fun stop() {
        if (!this::controller.isInitialized)
            return

        controller.stop()
    }
}

/*object Player {
    private val player = MediaPlayer()

    init {
        player.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
    }

     fun play(url: String) {
        if (url.isEmpty())
            return

        player.reset()

        try {
            player.setDataSource(url)
            player.prepare()
            player.setVolume(1.0F, 1.0F)
            player.start()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    fun pause() {
        if (player.isPlaying)
            player.pause()
    }

    fun resume() {
        if (!player.isPlaying)
            player.start()
    }

    fun stop() {
        player.stop()
    }
}*/