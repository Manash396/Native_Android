package com.example.chat.domain.chat

import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BlueToothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices :  StateFlow<List<BlueToothDevice>>
    val pairedDevices :  StateFlow<List<BlueToothDevice>>
    val error : SharedFlow<String>
    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer() : Flow<ConnectionResult>
    fun connectToDevice(device: BlueToothDevice) : Flow<ConnectionResult>
    fun closeConnection()

    suspend fun  trySendMessage(message: String) : BluetoothMessege?

    fun release()

}