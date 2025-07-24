package com.ancienty.huginproject.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ServerIpSection(
    serverIp: String,
    onServerIpChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Server IP:")
        OutlinedTextField(
            value = serverIp,
            onValueChange = onServerIpChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}