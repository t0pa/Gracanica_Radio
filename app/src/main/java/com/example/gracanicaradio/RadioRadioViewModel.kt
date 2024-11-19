package com.example.gracanicaradio

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RadioViewModel : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null

    private val streamUrl = "https://radiogracanica.wtvnet.net:9443/stream"

    var isPlaying by mutableStateOf(false)
        private set

    var isPrepared by mutableStateOf(false)
        private set

    var isBuffering by mutableStateOf(false)
        private set

    init {
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(streamUrl)
            prepareAsync()
            setOnPreparedListener {
                isPrepared = true
                isBuffering = false
            }
            setOnErrorListener { _, _, _ ->
                isBuffering = true
                false
            }
        }
    }

    fun playStream() {
        mediaPlayer?.let {
            if (!it.isPlaying && isPrepared) {
                it.start()
                isPlaying = true
                isBuffering = false


            }
        }
    }

    fun pauseStream() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}