package com.example.memorygameteam2.soundeffect

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.memorygameteam2.R

class SoundEffect(context: Context) {
    companion object {
        const val MAX_STREAMS = 5
        const val BUTTON = "button"
    }

    private var soundPool: SoundPool =
        SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()

    private val soundMap = mutableMapOf<String, Int>()

    init {
        loadSound(context, BUTTON, R.raw.buttonclick)
    }

    fun loadSound(
        context: Context,
        name: String,
        resId: Int,
    ) {
        val soundId = soundPool.load(context, resId, 1)
        soundMap[name] = soundId
    }

    fun play(
        name: String,
        volume: Float = 1f,
    ) {
        val soundId = soundMap[name]
        soundId?.let {
            soundPool.play(it, volume, volume, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
