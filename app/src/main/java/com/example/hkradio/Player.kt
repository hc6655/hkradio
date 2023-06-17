package com.example.hkradio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaSession2
import android.support.v4.media.session.MediaSessionCompat

object Player {
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
}