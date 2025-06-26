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

    override fun onCreate() {
        super.onCreate()
        soundEffect = SoundEffect(this)

        // Load sound effect to SoundEffect player
        soundEffect?.loadSound(this, SoundManager.BUTTON_CLICK, R.raw.buttonclick)
        // Load card flip sound
        soundEffect?.loadSound(this, SoundManager.CARD_FLIP,R.raw.flip_sound)
    }

    // Map of background music (if we adding more than 1)
    private var backgroundMusicList =
        mutableMapOf<String, Int>(
            Pair("Doki", R.raw.gamebg),
            Pair("SoHappy", R.raw.gamebg2),
        )

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            SoundManager.PLAY_BACKGROUND_MUSIC -> {
                if (mediaPlayer == null) {
                    playBackgroundMusic()
                }
            }

            SoundManager.STOP_BACKGROUND_MUSIC -> {
                stopBackgroundMusic()
            }

            SoundManager.RESUME_BACKGROUND_MUSIC -> {
                if (mediaPlayer != null) {
                    mediaPlayer?.start()
                } else if (getSharedPreferences("music", MODE_PRIVATE).getBoolean("isOn", false)) {
                    playBackgroundMusic()
                }
            }

            SoundManager.PAUSE_BACKGROUND_MUSIC -> {
                if (mediaPlayer != null) {
                    mediaPlayer?.pause()
                }
            }

            SoundManager.BUTTON_CLICK -> {
                soundEffect?.play(SoundManager.BUTTON_CLICK)
            }

            // for card flip
            SoundManager.CARD_FLIP -> {
                soundEffect?.play(SoundManager.CARD_FLIP)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun playBackgroundMusic() {
        var musicToPlay = backgroundMusicList[backgroundMusicList.keys.random()]
        if (musicToPlay == null) return
        mediaPlayer = MediaPlayer.create(this, musicToPlay)
        mediaPlayer?.setVolume(SoundManager.BACKGROUND_MUSIC_VOLUME, SoundManager.BACKGROUND_MUSIC_VOLUME)
        mediaPlayer?.setOnCompletionListener { playBackgroundMusic() }
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
