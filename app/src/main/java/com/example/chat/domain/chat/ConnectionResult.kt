package com.example.chat.domain.chat

import android.os.Message

sealed interface ConnectionResult {
    object  ConnectionEstablished : ConnectionResult

    data class TransferSucceeded(val message: BluetoothMessege) : ConnectionResult

    data class Error(val message: String) :  ConnectionResult
}