package com.example.memorygameteam2

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.memorygameteam2.soundeffect.SoundManager

/**
 * Following is to set listeners for the Application's lifecycle
 *
 * AppLifecycleListener is a GLOBAL lifecycle listener, but it only tracks if app is IN VIEW or OUT OF VIEW
 * ActivityListener is Activity-specific listener, tracking the whole activity lifecycle
 *
 * For now, only using this to stop background music (when app is minimised)
 *
 */
class MemoryGameApplication : Application(), LifecycleObserver {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())
        registerActivityLifecycleCallbacks(ActivityListener())
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
        SoundManager.controlBackgroundMusic(MemoryGameApplication.appContext, SoundManager.STOP_BACKGROUND_MUSIC)
    }
}

class ActivityListener : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        // Ignore Detekt
    }

    override fun onActivityStarted(activity: Activity) {
        // Ignore Detekt
    }

    override fun onActivityResumed(activity: Activity) {
        // Ignore Detekt
    }

    override fun onActivityPaused(activity: Activity) {
        // Ignore Detekt
    }

    override fun onActivityStopped(activity: Activity) {
        // Ignore Detekt
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {
        // Ignore Detekt
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Ignore Detekt
    }
}
