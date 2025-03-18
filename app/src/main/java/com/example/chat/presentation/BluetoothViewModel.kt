package com.example.chat.presentation

import android.os.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat.domain.chat.BlueToothController
import com.example.chat.domain.chat.BlueToothDevice
import com.example.chat.domain.chat.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val blueToothController: BlueToothController
): ViewModel(){
    private val _state = MutableStateFlow(BluetoothUIState())
    val state = combine(
        blueToothController.scannedDevices,
        blueToothController.pairedDevices,
        _state
    ){
        scannedDevices,pairedDevices,state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            message = if(state.isConnected) state.message else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(6000), _state.value)

    private var deviceConnectionJob: Job? = null

    init {

        blueToothController.isConnected.onEach {
            isConnected -> _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        blueToothController.error.onEach {
            error -> _state.update { it.copy(errorMessege = error) }
        }.launchIn(viewModelScope)

    }

    // show notification method
    fun shownotification(message : String){
        if(LifeCycleObserver.isAppinForeground){
            return
        }
       // first i have to get notification manager
      blueToothController.showNotification(message)
    }

    fun startScan(){
        blueToothController.startDiscovery()
    }

    fun stopScan(){
        blueToothController.stopDiscovery()
    }

    fun sendMessage(message: String){
        viewModelScope.launch{
            if (message == "") return@launch
            val bluetoothMessage = blueToothController.trySendMessage(message)
            if (bluetoothMessage != null){
                _state.update {
                    it.copy(
                      message = it.message + bluetoothMessage
                    )
                }
            }
        }
    }

    private fun Flow<ConnectionResult>.listen(): Job{
        return onEach {
            result ->
            when(result){

                ConnectionResult.ConnectionEstablished -> {
                    _state.update { it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessege = null
                    ) }
                }

                is ConnectionResult.TransferSucceeded ->{

                    _state.update {
                        it.copy(
                            message = it.message + result.message
                        )
                    }

                    shownotification(result.message.message)

                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessege = result.message
                        )
                    }
                }
            }
        }.catch {
            throwable ->
            blueToothController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                    errorMessege = null
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        blueToothController.release()
        super.onCleared()
    }

    fun connectToDevice(device: BlueToothDevice){
        _state.update { it.copy(
            isConnecting = true
        ) }
       deviceConnectionJob = blueToothController.connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice(){
        deviceConnectionJob?.cancel()
        blueToothController.closeConnection()
        _state.update { it.copy(
            isConnecting = false,
            isConnected = false
        ) }
    }

    fun waitForIncomingConnection(){
        _state.update {it.copy(
            isConnecting = true)
        }
        deviceConnectionJob = blueToothController.startBluetoothServer()
            .listen()
    }




}