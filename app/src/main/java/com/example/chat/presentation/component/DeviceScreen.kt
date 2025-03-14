package com.example.chat.presentation.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chat.domain.chat.BlueToothDevice
import com.example.chat.presentation.BluetoothUIState
import androidx.compose.ui.unit.sp

@Composable
fun DeviceScreen(
    state: BluetoothUIState,
    onStartScan : () -> Unit,
    onStopScan :  ()  -> Unit,
    onStartServer: () -> Unit,
    onConnectToDevice:(BlueToothDevice) -> Unit ,

){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.statusBars.asPaddingValues()
            )
    ){


        BluetoothDeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onConnectToDevice,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row (
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            horizontalArrangement = Arrangement.SpaceAround

        ){
            Button(onClick = onStartScan) {
                Text(text = "Start Scan", fontSize = 12.sp)
            }

            Button(onClick = onStopScan
                ) {
                Text(text = "Stop Scan", fontSize = 12.sp)
            }

            Button(onClick = onStartServer) {
                Text(text = "Start Server", fontSize = 12.sp)
            }


        }
    }
}

@Composable
fun BluetoothDeviceList(
    pairedDevices : List<BlueToothDevice>,
    scannedDevices : List<BlueToothDevice>,
    onClick : (BlueToothDevice) ->Unit,
    modifier: Modifier = Modifier
){
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item{
            Text(
                text = "Paired Devices ",
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                modifier = Modifier.padding(15.dp)
            )
        }
        items(pairedDevices){
            device ->
            Text(
                text = (device.name + " : " + device.address) ?: "No name",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .clickable{onClick(device)}
            )
        }

        item{
            Text(
                text = "Scanned Devices ",
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                modifier = Modifier.padding(15.dp)

            )
        }
        items(scannedDevices){
                device ->
            Text(
                text = (device.name + " : " + device.address) ?:  "No name",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .clickable{onClick(device)}
            )
        }


    }
}
