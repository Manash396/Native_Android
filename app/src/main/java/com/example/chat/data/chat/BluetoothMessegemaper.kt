package com.example.chat.data.chat

import com.example.chat.domain.chat.BluetoothMessege

fun String.toBluetoothMessege(isFromLocalUser: Boolean): BluetoothMessege{
    val sender = substringBeforeLast("#")
    val message = substringAfter("#")
    return BluetoothMessege(
        message = message,
        sender = sender,
        isFromLocalUser = isFromLocalUser
    )
}

fun BluetoothMessege.toByteArray() : ByteArray{
    return "$sender#$message".encodeToByteArray()
}