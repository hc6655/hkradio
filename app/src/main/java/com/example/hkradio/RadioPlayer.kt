package com.example.hkradio

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController

object RadioPlayer {
    private lateinit var controller: MediaController
    private var cachedData: ChannelData? = null

    var isPlaying = MutableLiveData(false)
    var isPlayerLoading = MutableLiveData(false)
    var isPause = MutableLiveData(false)

    fun setController(controller: MediaController) {
        this.controller = controller

        this.controller.addListener(object: Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)

                isPlaying.value = player.isPlaying
                isPlayerLoading.value = player.isLoading
            }
        })
    }

    fun play(data: ChannelData) {
        if (!this::controller.isInitialized)
            return

        cachedData = data

        isPause.value = false

        val media = MediaItem.Builder()
            .setMediaId(data.link)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(data.name)
                    .setArtist(data.artist)
                    .setArtworkUri(Uri.parse("android.resource://com.example.hkradio/" + data.artwork))
                    .build()
            ).build()
        controller.setMediaItem(media)
        //controller.prepare()
        controller.play()
    }

    fun stop() {
        if (!this::controller.isInitialized)
            return

        controller.stop()
    }

    fun toggle() {
        if (!this::controller.isInitialized)
            return

        if (controller.isPlaying) {
            isPause.value = true
            controller.pause()
        }
        else {
            cachedData?.let { play(it) }
        }

    }
}