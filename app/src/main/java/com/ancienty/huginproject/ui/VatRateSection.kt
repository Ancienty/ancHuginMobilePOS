package com.ancienty.huginproject.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ancienty.huginproject.database.Product

@Composable
fun VatRateSection(
    vatRate: Int,
    products: List<Product>,
    onProductClick: (Product) -> Unit
) {
    Column {
        Text(text = "VAT $vatRate% (${products.size} products)", style = MaterialTheme.typography.subtitle1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            products.take(4).forEach { p ->
                Button(
                    onClick = { onProductClick(p) },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(80.dp)
                        .height(80.dp)
                ) {
                    Text(text = p.name, maxLines = 2)
                }
            }
            if (products.size > 4) {
                products.drop(4).forEach { p ->
                    Button(
                        onClick = { onProductClick(p) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(80.dp)
                            .height(80.dp)
                    ) {
                        Text(text = p.name, maxLines = 2)
                    }
                }
            }
        }
    }
}