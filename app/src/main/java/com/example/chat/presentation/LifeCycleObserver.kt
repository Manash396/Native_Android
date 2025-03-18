package com.example.chat.presentation

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

object LifeCycleObserver : DefaultLifecycleObserver{
  var isAppinForeground = false
    private set


  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)
    isAppinForeground = true
  }

  override fun onStop(owner: LifecycleOwner) {
    super.onStop(owner)
    isAppinForeground = false
  }

  fun register(){
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
  }

}