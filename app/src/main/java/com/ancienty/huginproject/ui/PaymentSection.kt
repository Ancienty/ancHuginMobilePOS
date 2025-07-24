package com.ancienty.huginproject.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PaymentSection(
    onCancel: () -> Unit,
    onPayment: (PaymentType) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button(onClick = onCancel) { Text("Cancel") }
        Button(onClick = { onPayment(PaymentType.Cash) }) { Text("Cash") }
        Button(onClick = { onPayment(PaymentType.Credit) }) { Text("Credit") }
        Button(onClick = { onPayment(PaymentType.Coupon) }) { Text("Coupon") }
    }
}