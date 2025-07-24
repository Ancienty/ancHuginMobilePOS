package com.ancienty.huginproject.models

import com.ancienty.huginproject.database.Product

data class BasketItem (
    val product: Product,
    val quantity: Int,
    val pricePerUnit: Double
) {
    val totalAmount: Double
        get() = quantity * pricePerUnit
}