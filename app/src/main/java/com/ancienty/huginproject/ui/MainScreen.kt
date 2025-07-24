package com.ancienty.huginproject.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ancienty.huginproject.models.BasketItem
import com.ancienty.huginproject.database.Product

enum class PaymentType { Cancel, Cash, Credit, Coupon }

@Composable
fun MainScreen(
    productsByVat: Map<Int, List<Product>>,
    basketItems: List<BasketItem>,
    onSellItem: (Product, Int, Double) -> Unit,
    onCancel: () -> Unit,
    onPayment: (PaymentType) -> Unit,
    serverIp: String,
    onServerIpChange: (String) -> Unit,
    onTestReport: () -> Unit
) {
    var showDialogFor by remember { mutableStateOf<Product?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 4 rows for each VAT rate
        listOf(0, 1, 10, 20).forEach { vat ->
            VatRateSection(
                vatRate = vat,
                products = productsByVat[vat] ?: emptyList(),
                onProductClick = { showDialogFor = it }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Basket summary
        BasketSummarySection(basketItems)

        Spacer(Modifier.height(16.dp))

        // Payment buttons
        PaymentSection(onCancel, onPayment)

        Spacer(Modifier.height(16.dp))

        // Test report button
        Button(
            onClick = onTestReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Test Report")
        }

        Spacer(Modifier.weight(1f))

        // Server IP editor
        ServerIpSection(serverIp, onServerIpChange)
    }

    // Sell‑item dialog
    showDialogFor?.let { product ->
        SellItemDialog(
            product = product,
            onDismiss = { showDialogFor = null },
            onConfirm = { qty, price ->
                onSellItem(product, qty, price)
                showDialogFor = null
            }
        )
    }
}