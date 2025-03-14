package com.example.chat.di

import android.content.Context
import com.example.chat.data.chat.AndroidBlueToothController
import com.example.chat.domain.chat.BlueToothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton

    fun provideBluetoothController(@ApplicationContext context : Context) : BlueToothController{
        return AndroidBlueToothController(context)
    }
}