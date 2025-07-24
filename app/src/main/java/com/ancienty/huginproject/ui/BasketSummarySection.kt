package com.ancienty.huginproject.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ancienty.huginproject.models.BasketItem
import java.text.NumberFormat

@Composable
fun BasketSummarySection(items: List<BasketItem>) {
    val last = items.lastOrNull()
    val total = items.sumOf { it.totalAmount }
    val fmt = NumberFormat.getCurrencyInstance()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (last != null) {
                Text("${last.quantity} x ${last.product.name}")
                Text(fmt.format(last.totalAmount))
            } else {
                Text("-")
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total:")
            Text(fmt.format(total))
        }
    }
}