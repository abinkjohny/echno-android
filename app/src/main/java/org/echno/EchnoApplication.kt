package org.echno

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Process-wide entry point. Android creates this once, before any screen.
 *
 * [HiltAndroidApp] generates the root dependency container, the Android counterpart
 * of Spring's ApplicationContext.
 */
@HiltAndroidApp
class EchnoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Logs only in debug builds; release builds stay silent so nothing sensitive leaks into logcat.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
