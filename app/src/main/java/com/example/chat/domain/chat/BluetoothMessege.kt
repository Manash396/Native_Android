package com.example.chat.domain.chat

data class BluetoothMessege(
    val message : String,
    val sender : String,
    val isFromLocalUser: Boolean
)
