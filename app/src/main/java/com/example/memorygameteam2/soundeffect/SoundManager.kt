package com.example.memorygameteam2.soundeffect

import android.content.Context
import android.content.Intent

/**
 * Sound controller
 * play/stop background music
 * play button sound effects
 *
 * Anything related to playing or stopping sounds
 */
object SoundManager {
    const val PLAY_BACKGROUND_MUSIC = "play_background"
    const val STOP_BACKGROUND_MUSIC = "stop_background"
    const val RESUME_BACKGROUND_MUSIC = "resume_background"
    const val PAUSE_BACKGROUND_MUSIC = "pause_background"
    const val BUTTON_CLICK = "button_click"

    fun controlBackgroundMusic(
        context: Context,
        action: String,
    ) {
        val intent =
            Intent(context, SoundService::class.java)
                .setAction(action)
        context.startService(intent)
    }

    fun playButtonClick(context: Context) {
        val intent =
            Intent(context, SoundService::class.java)
                .setAction(BUTTON_CLICK)
        context.startService(intent)
    }
}
