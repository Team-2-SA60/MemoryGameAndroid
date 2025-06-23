package com.example.memorygameteam2.soundeffect

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Sound Effect Player
 *
 * Usage:
 * loadSound(context, "name_used_to_call_sound", ResourceId)
 * play("name_used_to_call_sound")
 */
class SoundEffect(context: Context) {
    private var soundPool: SoundPool =
        SoundPool.Builder()
            .setMaxStreams(SoundManager.SOUND_EFFECT_MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()

    private val soundMap = mutableMapOf<String, Int>()

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
