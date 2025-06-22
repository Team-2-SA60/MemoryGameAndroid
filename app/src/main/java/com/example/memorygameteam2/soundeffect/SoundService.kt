package com.example.memorygameteam2.soundeffect

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.example.memorygameteam2.R

/**
 * Sound service to play the application sounds (sound effect and background music)
 *
 */
class SoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var soundEffect: SoundEffect? = null

    companion object {
        const val BACKGROUND_VOLUME = 0.5F
    }

    override fun onCreate() {
        super.onCreate()
        soundEffect = SoundEffect(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            SoundManager.PLAY_BACKGROUND_MUSIC -> {
                if (mediaPlayer == null) {
                    playBackgroundMusic(R.raw.gamebg)
                }
            }

            SoundManager.STOP_BACKGROUND_MUSIC -> {
                stopBackgroundMusic()
            }

            SoundManager.RESUME_BACKGROUND_MUSIC -> {
                if (mediaPlayer != null) {
                    mediaPlayer?.start()
                } else if (getSharedPreferences("music", MODE_PRIVATE).getBoolean("isOn", false)) {
                    playBackgroundMusic(R.raw.gamebg)
                }
            }

            SoundManager.PAUSE_BACKGROUND_MUSIC -> {
                if (mediaPlayer != null) {
                    mediaPlayer?.pause()
                }
            }

            SoundManager.BUTTON_CLICK -> {
                soundEffect?.play(SoundEffect.BUTTON)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun playBackgroundMusic(song: Int) {
        mediaPlayer = MediaPlayer.create(this, song)
        mediaPlayer?.setVolume(BACKGROUND_VOLUME, BACKGROUND_VOLUME)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun stopBackgroundMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopBackgroundMusic()
        soundEffect?.release()
        soundEffect = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
