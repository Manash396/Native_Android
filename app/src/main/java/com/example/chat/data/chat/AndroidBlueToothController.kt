package com.example.chat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import com.example.chat.domain.chat.BlueToothController
import com.example.chat.domain.chat.BlueToothDevice
import com.example.chat.domain.chat.BluetoothDeviceDomain
import com.example.chat.domain.chat.BluetoothMessege
import com.example.chat.domain.chat.BluetoothPermissionHandler
import com.example.chat.domain.chat.ConnectionResult
import com.example.chat.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID


@SuppressLint("MissingPermission")

class AndroidBlueToothController(private val context: Context) : BlueToothController {
//    getting bluetooth services for Android sdk


    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
}
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

     private var bluetoothDataTransferService : BluetoothDataTransferService? = null

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BlueToothDevice>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BlueToothDevice>>
        get() = _pairedDevices.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    override val error: SharedFlow<String>
        get() = _error.asSharedFlow()

    private val foundDeviceReciever = FoundDeviceReciever{
        device -> _scannedDevices.update{devices ->
            val newDevice = device.toBlueToothDeviceDomain()
        if(newDevice in devices ) devices else devices + newDevice
    }

    }

   private val bluetoothStateReciever = BluetoothStateReciever{
       isConnected , bluetoothDevice ->
       if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true){
           _isConnected.update { isConnected }
       }else{
           CoroutineScope(Dispatchers.IO).launch{
               _error.emit("Can't connect to the non-paired device")
           }
       }
   }

    private var currentServerSocket : BluetoothServerSocket? = null
    private var  currentClientSocket : BluetoothSocket? = null

    init{
        updatePairedDevices()
        context.registerReceiver(bluetoothStateReciever,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            )
    }


    override fun startDiscovery() {

        if(!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)){
           return
        }

        context.registerReceiver(
            foundDeviceReciever,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

       updatePairedDevices()
        _scannedDevices.value = emptyList()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
       if(!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)){
           return
       }
        bluetoothAdapter?.cancelDiscovery()

    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                throw SecurityException("No BLUETOOTH_CONNECT Permission ")
            }
         currentServerSocket =  bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(Service_uuid)
            )


               currentClientSocket =   try {
                      currentServerSocket?.accept().also {
                          emit(ConnectionResult.ConnectionEstablished)

                      }

                  }catch (e : IOException){
                      null
                  }


            currentClientSocket?.let { currentServerSocket?.close()
              val service = BluetoothDataTransferService(it)
             bluetoothDataTransferService = service
                emitAll(service.listenForIncomingMessages()
                    .map {
                        ConnectionResult.TransferSucceeded(it)
                    }
                )
            }
        }.onCompletion{
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BlueToothDevice): Flow<ConnectionResult> {
         return flow {
             if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                 throw SecurityException("No BLUETOOTH_CONNECT permission")
             }

             val bluetoothDevice = bluetoothAdapter
                 ?.getRemoteDevice(device.address)

             currentClientSocket = bluetoothDevice
                 ?.createRfcommSocketToServiceRecord(
                     UUID.fromString(Service_uuid)
                 )

             stopDiscovery()
             currentClientSocket?.let {
                 socket ->
                  try {
                      socket.connect()
                      emit(ConnectionResult.ConnectionEstablished)
                      BluetoothDataTransferService(socket).also {
                          bluetoothDataTransferService = it
                          emitAll(
                              it.listenForIncomingMessages()
                                  .map { ConnectionResult.TransferSucceeded(it) }
                          )
                      }
                  }catch (e : IOException){
                      socket.close()
                      currentClientSocket = null
                      emit(ConnectionResult.Error("Connection was interrupted"))
                  }
             }

         }.onCompletion {
             closeConnection()
         }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }


    override fun release() {
        context.unregisterReceiver(foundDeviceReciever)
        context.unregisterReceiver(bluetoothStateReciever)
        closeConnection()
    }


    private fun updatePairedDevices(){
        if(!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) return
        bluetoothAdapter
            ?.bondedDevices
            ?.map{ it.toBlueToothDeviceDomain()}
            ?.also{devices -> _pairedDevices.update { devices }}
    }

    private fun hasPermission(permission: String ) : Boolean{
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun trySendMessage(message: String): BluetoothMessege? {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
            return null
        }
        if (bluetoothDataTransferService == null){
            return null
        }
        val bluetoothMessage = BluetoothMessege(
            message = message,
            sender = bluetoothAdapter?.name ?: "Unknown Name",
            isFromLocalUser = true
        )

        bluetoothDataTransferService?.sendMessage(bluetoothMessage.toByteArray())
        return bluetoothMessage
    }

   override fun showNotification(message: String){
       // creating a chennel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =    NotificationChannel(
                "channel_name",
               "channel_id",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(
           context,"channel_id"
        ).setContentTitle("New Message")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .build()

        notificationManager.notify(1,notification)



    }

    companion object{
        const val Service_uuid = "282b4f76-aa95-4d7d-91f9-2cadc2baa727"
    }
}