package com.example.memorygameteam2.soundeffect

import android.content.Context
import android.media.MediaPlayer
import com.example.memorygameteam2.R

object SoundEffectPlayer {
    fun buttonClick(context: Context) {
        val clickSound = MediaPlayer.create(context, R.raw.buttonclick)
        clickSound.setVolume(1F, 1F)
        clickSound.setOnCompletionListener {
            it.release()
        }
        clickSound.start()
    }
}
