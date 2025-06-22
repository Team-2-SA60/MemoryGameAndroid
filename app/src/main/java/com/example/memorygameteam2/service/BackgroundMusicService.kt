package com.example.memorygameteam2.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

/**
 * Service to start automatically when application launches
 * Plays the background music on infinite loop
 */
class BackgroundMusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val MEDIA_VOLUME = 0.5F
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            "play" -> {
                val song = intent.getIntExtra("song", 0)
                mediaPlayer = MediaPlayer.create(this, song)
                mediaPlayer?.setVolume(MEDIA_VOLUME, MEDIA_VOLUME)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
