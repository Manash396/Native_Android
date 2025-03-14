package com.example.chat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.chat.domain.chat.BluetoothDeviceDomain

@SuppressLint("MissingPermission")

fun BluetoothDevice.toBlueToothDeviceDomain() : BluetoothDeviceDomain{
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}