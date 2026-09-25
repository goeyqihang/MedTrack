package com.qihang.medtrack

import android.app.Application

class MedTrackApplication : Application() {

    /** Shared dependencies; see [AppContainer]. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
