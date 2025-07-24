package com.ancienty.huginproject.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DAO {

    // ======= PRODUCT OPERATIONS =======

    @Query("SELECT * FROM product")
    suspend fun getAllProducts(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(vararg products: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    // ======= RECEIPT OPERATIONS =======

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt)

    @Query("""
        SELECT * FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    suspend fun getReceiptsByHour(hourPrefix: String): List<Receipt>

    @Query("""
        SELECT COUNT(*) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
          AND paymentType <> 0
    """)
    fun countReceiptsByHourFlow(hourPrefix: String): Flow<Int>

    @Query("""
        SELECT SUM(amountVat0) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun sumVat0ByHourFlow(hourPrefix: String): Flow<Double>

    @Query("""
        SELECT SUM(amountVat1) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun sumVat1ByHourFlow(hourPrefix: String): Flow<Double>

    @Query("""
        SELECT SUM(amountVat10) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun sumVat10ByHourFlow(hourPrefix: String): Flow<Double>

    @Query("""
        SELECT SUM(amountVat20) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun sumVat20ByHourFlow(hourPrefix: String): Flow<Double>

    @Query("""
        SELECT SUM(amountVat0 + amountVat1 + amountVat10 + amountVat20)
        FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun totalAmountByHourFlow(hourPrefix: String): Flow<Double>

    @Query("""
        SELECT COUNT(*) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun totalReceiptCountByHourFlow(hourPrefix: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
          AND paymentType = :type
    """)
    fun countByPaymentTypeFlow(hourPrefix: String, type: Int): Flow<Int>

    @Query("""
        SELECT SUM(
            CASE WHEN paymentType = :type
                 THEN (amountVat0 + amountVat1 + amountVat10 + amountVat20)
                 ELSE 0 END
        )
        FROM receipt
        WHERE receiptDateTime LIKE :hourPrefix || '%'
    """)
    fun sumByPaymentTypeFlow(hourPrefix: String, type: Int): Flow<Double>
}