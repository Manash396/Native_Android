package com.example.chat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.chat.presentation.LifeCycleObserver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BluetoothApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LifeCycleObserver.register()
    }
}