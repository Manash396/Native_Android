package com.example.chat.presentation.component

import android.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chat.domain.chat.BluetoothMessege
import com.example.chat.ui.theme.Black
import com.example.chat.ui.theme.ChatTheme
import com.example.chat.ui.theme.Pink40
import com.example.chat.ui.theme.Pink80
import com.example.chat.ui.theme.Purple40
import com.example.chat.ui.theme.Purple80

@Composable
fun ChatMessage(
    message: BluetoothMessege,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (message.isFromLocalUser) 15.dp else 0.dp,
                    topEnd =  15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (message.isFromLocalUser) 0.dp else 15.dp
                )
            )
            .background(
                if (message.isFromLocalUser) Pink80 else Purple80
            )
            .padding(15.dp)
    ) {
        Text(
            text = message.sender,
            fontSize = 10.sp,
            color = Black
        )


        Text(
            text = message.message,
            color = Black,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    }

}

@Preview
@Composable
private fun ChatMessagePreview() {
    ChatTheme {
        ChatMessage(
            message = BluetoothMessege(
                message = "Hello Manash",
                sender = "Krishna",
                isFromLocalUser = true
            )
        )
    }
}