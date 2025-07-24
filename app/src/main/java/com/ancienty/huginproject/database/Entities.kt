package com.ancienty.huginproject.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "vatRate")
    val vatRate: Int,

    @ColumnInfo(name = "price")
    val price: Double
)


@Entity(tableName = "receipt")
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,

    @ColumnInfo(name = "receiptNumber")
    val receiptNumber: Int,

    @ColumnInfo(name = "receiptDateTime")
    val receiptDateTime: String,

    @ColumnInfo(name = "amountVat0")
    val amountVat0: Double,

    @ColumnInfo(name = "amountVat1")
    val amountVat1: Double,

    @ColumnInfo(name = "amountVat10")
    val amountVat10: Double,

    @ColumnInfo(name = "amountVat20")
    val amountVat20: Double,

    @ColumnInfo(name = "paymentType")
    val paymentType: Int
)
