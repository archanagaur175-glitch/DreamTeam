package com.dreamteam.app

import android.app.Application
import com.dreamteam.feature.smartalarm.alarm.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DreamTeamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
