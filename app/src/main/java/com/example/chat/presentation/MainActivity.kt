package com.example.chat.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.chat.ui.theme.ChatTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chat.domain.chat.BluetoothPermissionHandler
import com.example.chat.presentation.component.ChatScreen
import com.example.chat.presentation.component.DeviceScreen



@AndroidEntryPoint
class MainActivity : ComponentActivity() , BluetoothPermissionHandler
{

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothenable : Boolean
        get() = bluetoothAdapter?.isEnabled == true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

      requestBluetoothPermission()


        setContent {
            ChatTheme {
                val viewModel = hiltViewModel<BluetoothViewModel>()
                val state  =  viewModel.state.collectAsState().value

                LaunchedEffect(key1 = state.errorMessege) {
                    state.errorMessege?.let { messege ->
                        Toast.makeText(
                            applicationContext,
                            messege,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }

                LaunchedEffect(key1=state.isConnected) {
                    if (state.isConnected){
                        Toast.makeText(
                            applicationContext,
                            "Device is connected!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                Surface (
                    color = MaterialTheme.colorScheme.background

                ){
                    when{
                        state.isConnecting ->{
                            Column(modifier = Modifier.fillMaxSize(),
                                  horizontalAlignment = Alignment.CenterHorizontally,
                                  verticalArrangement = Arrangement.Center
                                ){
                                CircularProgressIndicator()
                                Text(text = "Connecting...")
                            }
                        }
                        state.isConnected -> {
                            ChatScreen(
                                state = state,
                                onDisconnect = viewModel :: disconnectFromDevice ,
                                onSendMessage = viewModel :: sendMessage,
                            )
                        }
                          else ->{
                              DeviceScreen(
                                  state = state,
                                  onStartScan = viewModel :: startScan,
                                  onStopScan = viewModel :: stopScan,
                                  onStartServer = viewModel :: waitForIncomingConnection,
                                  onConnectToDevice = viewModel :: connectToDevice
                              )
                          }
                    }




                }

            }
        }
    }

    override fun requestBluetoothPermission() {
        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
//                    not needed
        }

        val permissionlauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){ perms ->
            val calEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                perms[Manifest.permission.BLUETOOTH_SCAN] == true
            }else true

            if(calEnableBluetooth && !isBluetoothenable){
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionlauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            }
        }
    }





}
