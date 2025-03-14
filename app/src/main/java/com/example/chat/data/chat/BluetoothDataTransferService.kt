package com.example.chat.data.chat

import android.bluetooth.BluetoothSocket
import com.example.chat.domain.chat.BluetoothMessege
import com.example.chat.domain.chat.ConnectionResult
import com.example.chat.domain.chat.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService (
    private val socket: BluetoothSocket
){
    fun listenForIncomingMessages():Flow<BluetoothMessege>{
         return flow{
             if(!socket.isConnected){
                 return@flow
             }
             val buffer = ByteArray(1024)
             while (true){
                 val byteCount = try {
                     socket.inputStream.read(buffer)
                 }catch (e : IOException){
                      throw TransferFailedException()
                 }
                 emit(
                    buffer.decodeToString(0,
                             endIndex = byteCount
                         ).toBluetoothMessege(
                             isFromLocalUser = false
                         )
                     )

             }
         }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO){
            try {
                socket.outputStream.write(bytes)
            }catch (e: IOException){
                e.printStackTrace()
                return@withContext false
            }
            true
        }
    }
}