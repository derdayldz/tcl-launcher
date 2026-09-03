package com.derda.tclauncher

import android.app.Application
import com.derda.tclauncher.data.LauncherRepository

class TvLauncherApp : Application() {
    lateinit var repository: LauncherRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = LauncherRepository(this)
    }
}
