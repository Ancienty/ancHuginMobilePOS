package com.ancienty.huginproject.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ancienty.huginproject.database.Product

@Composable
fun SellItemDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, price: Double) -> Unit
) {
    var qtyText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(product.price.toString()) }
    
    val qty = qtyText.toIntOrNull() ?: 1
    val price = priceText.toDoubleOrNull() ?: 0.01
    val isValidQty = qty in 1..99
    val isValidPrice = price in 0.01..999.99

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sell ${product.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity (1-99)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValidQty
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price per unit (0.01-999.99)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValidPrice
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidQty && isValidPrice) {
                        onConfirm(qty, price)
                    }
                },
                enabled = isValidQty && isValidPrice
            ) { Text("Add") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}