package com.example.chat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class FoundDeviceReciever (
    private val onDeviceFound:(BluetoothDevice) -> Unit
) : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
        BluetoothDevice.ACTION_FOUND -> {
            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION") // Suppresses the warning for older versions
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
            device?.let { onDeviceFound(it) }
        }

        }
    }
}