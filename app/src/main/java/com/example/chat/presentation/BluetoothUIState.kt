package com.example.chat.presentation

import com.example.chat.domain.chat.BlueToothDevice
import com.example.chat.domain.chat.BluetoothMessege

data class BluetoothUIState(
    val scannedDevices : List<BlueToothDevice> = emptyList(),
    val pairedDevices : List<BlueToothDevice> = emptyList(),
    val isConnected : Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessege : String? = null,
    val message: List<BluetoothMessege> = emptyList()
    )
