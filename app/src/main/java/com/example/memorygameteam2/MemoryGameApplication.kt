package com.example.memorygameteam2

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.memorygameteam2.soundeffect.SoundManager

class MemoryGameApplication : Application(), LifecycleObserver {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())
    }
}

class AppLifecycleListener : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        // App enters foreground
        Log.d("AppLifecycle", "App in foreground")
        SoundManager.controlBackgroundMusic(MemoryGameApplication.appContext, SoundManager.RESUME_BACKGROUND_MUSIC)
    }

    override fun onStop(owner: LifecycleOwner) {
        // App goes to background
        Log.d("AppLifecycle", "App in background")
        SoundManager.controlBackgroundMusic(MemoryGameApplication.appContext, SoundManager.PAUSE_BACKGROUND_MUSIC)
    }
}
