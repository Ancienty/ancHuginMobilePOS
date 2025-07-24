package com.ancienty.huginproject

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.lifecycleScope
import com.ancienty.huginproject.database.AppDatabase
import com.ancienty.huginproject.database.DAO
import com.ancienty.huginproject.database.Product
import com.ancienty.huginproject.database.Receipt
import com.ancienty.huginproject.models.BasketItem
import com.ancienty.huginproject.ui.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var dao: DAO
    private var serverIp: String = "192.168.50.175"
    private val reportedHours = mutableSetOf<String>()
    private val decimalFormat = DecimalFormat("0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.getInstance(applicationContext).retailDao()
        setContent {
            MaterialTheme {
                var allProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
                var currentServerIp by remember { mutableStateOf(serverIp) }
                val basketItems = remember { mutableStateListOf<BasketItem>() }

                LaunchedEffect(Unit) {
                    try {
                        Log.d("HuginDB", "Starting database initialization...")
                        val products = withContext(Dispatchers.IO) { dao.getAllProducts() }
                        Log.d("HuginDB", "Found ${products.size} existing products")
                        
                        if (products.isEmpty()) {
                            Log.d("HuginDB", "No products found, inserting sample data...")
                            withContext(Dispatchers.IO) {
                                insertSampleData()
                            }
                            Log.d("HuginDB", "Sample data insertion completed")
                            
                            val newProducts = withContext(Dispatchers.IO) { dao.getAllProducts() }
                            Log.d("HuginDB", "After insertion: ${newProducts.size} products found")
                            allProducts = newProducts
                        } else {
                            Log.d("HuginDB", "Using existing products")
                            allProducts = products
                        }
                    } catch (e: Exception) {
                        Log.e("HuginDB", "Database initialization error", e)
                    }
                }

                val productsByVat by remember(allProducts) {
                    derivedStateOf {
                        mapOf(
                            0 to allProducts.filter { it.vatRate == 0 },
                            1 to allProducts.filter { it.vatRate == 1 },
                            10 to allProducts.filter { it.vatRate == 10 },
                            20 to allProducts.filter { it.vatRate == 20 }
                        )
                    }
                }

                MainScreen(
                    productsByVat = productsByVat,
                    basketItems = basketItems,
                    onSellItem = { product, quantity, pricePerUnit ->
                        basketItems.add(BasketItem(product, quantity, pricePerUnit))
                    },
                    onCancel = {
                        saveReceipt(basketItems.toList(), 0)
                        basketItems.clear()
                    },
                    onPayment = { paymentType ->
                        saveReceipt(basketItems.toList(), paymentType.ordinal)
                        basketItems.clear()
                    },
                    serverIp = currentServerIp,
                    onServerIpChange = { newIp ->
                        currentServerIp = newIp
                        serverIp = newIp
                    },
                    onTestReport = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            sendTestReport(currentServerIp)
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(60000)
                        if (basketItems.isEmpty()) {
                            withContext(Dispatchers.IO) {
                                checkAndSendHourlyReport(serverIp)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun insertSampleData() {
        try {
            Log.d("HuginDB", "Creating sample products...")
            val products = arrayOf(
                Product(1, "Karpuz", 0, 5.65),
                Product(2, "Armut", 0, 5.65),
                Product(3, "Elma", 0, 5.65),
                Product(4, "Ananas", 0, 5.65),
                Product(5, "Kiraz", 0, 5.65),
                Product(6, "Kavun", 1, 5.65),
                Product(7, "Muz", 10, 5.65),
                Product(8, "Erik", 10, 5.65),
                Product(9, "Domates", 20, 5.65),
                Product(10, "Salatalık", 20, 5.65)
            )
            
            Log.d("HuginDB", "Inserting ${products.size} products using insertAllProducts...")
            dao.insertAllProducts(*products)
            Log.d("HuginDB", "All sample products inserted successfully")
            
            // Verify insertion
            val count = dao.getAllProducts().size
            Log.d("HuginDB", "Verification: Database now contains $count products")
        } catch (e: Exception) {
            Log.e("HuginDB", "Error inserting sample data", e)
            e.printStackTrace()
        }
    }

    private fun saveReceipt(items: List<BasketItem>, paymentType: Int) {
        if (items.isEmpty()) return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("HuginDB", "Saving receipt with ${items.size} items, payment type: $paymentType")
                val sumsByVat = items.groupBy { it.product.vatRate }
                    .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
                val now = LocalDateTime.now()
                val hourPrefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))
                val receiptsThisHour = dao.getReceiptsByHour(hourPrefix).size
                val receiptNumber = receiptsThisHour + 1
                val receipt = Receipt(
                    receiptNumber = receiptNumber,
                    receiptDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    amountVat0 = sumsByVat[0] ?: 0.0,
                    amountVat1 = sumsByVat[1] ?: 0.0,
                    amountVat10 = sumsByVat[10] ?: 0.0,
                    amountVat20 = sumsByVat[20] ?: 0.0,
                    paymentType = paymentType
                )
                dao.insertReceipt(receipt)
                Log.d("HuginDB", "Receipt saved: #$receiptNumber, total: ${receipt.amountVat0 + receipt.amountVat1 + receipt.amountVat10 + receipt.amountVat20}")
                checkAndSendHourlyReport(serverIp)
            } catch (e: Exception) {
                Log.e("HuginDB", "Error saving receipt", e)
            }
        }
    }

    private suspend fun checkAndSendHourlyReport(serverIp: String) {
        val now = LocalDateTime.now()
        val reportHour = now.minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))
        if (reportedHours.contains(reportHour)) return
        val receipts = dao.getReceiptsByHour(reportHour)
        if (receipts.isEmpty()) {
            reportedHours.add(reportHour)
            return
        }
        val valid = receipts.filter { it.paymentType != 0 }
        val cancel = receipts.filter { it.paymentType == 0 }
        val receiptCount = receipts.size
        val totalAmount = valid.sumOf { it.amountVat0 + it.amountVat1 + it.amountVat10 + it.amountVat20 }
        val canceledCount = cancel.size
        val canceledAmount = cancel.sumOf { it.amountVat0 + it.amountVat1 + it.amountVat10 + it.amountVat20 }
        val vat0 = valid.sumOf { it.amountVat0 }
        val vat1 = valid.sumOf { it.amountVat1 }
        val vat10 = valid.sumOf { it.amountVat10 }
        val vat20 = valid.sumOf { it.amountVat20 }
        val byType = valid.groupBy { it.paymentType }
            .mapValues { group -> group.value.sumOf { it.amountVat0 + it.amountVat1 + it.amountVat10 + it.amountVat20 } }
        val xml = "<hourlyReport>" +
                "<reportHour>$reportHour</reportHour>" +
                "<receiptCount>$receiptCount</receiptCount>" +
                "<totalAmount>${decimalFormat.format(totalAmount)}</totalAmount>" +
                "<canceledInfo><count>$canceledCount</count><amount>${decimalFormat.format(canceledAmount)}</amount></canceledInfo>" +
                "<salesVatDistribution>" +
                "<sales><vatRate>0</vatRate><amount>${decimalFormat.format(vat0)}</amount></sales>" +
                "<sales><vatRate>1</vatRate><amount>${decimalFormat.format(vat1)}</amount></sales>" +
                "<sales><vatRate>10</vatRate><amount>${decimalFormat.format(vat10)}</amount></sales>" +
                "<sales><vatRate>20</vatRate><amount>${decimalFormat.format(vat20)}</amount></sales>" +
                "</salesVatDistribution>" +
                "<paymentDistribution>" +
                "<payment><paymentType>Cash</paymentType><amount>${decimalFormat.format(byType[1] ?: 0.0)}</amount></payment>" +
                "<payment><paymentType>Credit</paymentType><amount>${decimalFormat.format(byType[2] ?: 0.0)}</amount></payment>" +
                "<payment><paymentType>Coupon</paymentType><amount>${decimalFormat.format(byType[3] ?: 0.0)}</amount></payment>" +
                "</paymentDistribution>" +
                "</hourlyReport>"
        val url = URL("http://$serverIp:4478")
        (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/xml; charset=UTF-8")
            outputStream.use { it.write(xml.toByteArray()) }
            if (responseCode == 200) {
                reportedHours.add(reportHour)
            }
        }
    }

    private suspend fun sendTestReport(serverIp: String) {
        try {
            Log.d("HuginDB", "Sending test report to $serverIp")
            val now = LocalDateTime.now()
            val reportHour = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))
            val receipts = dao.getReceiptsByHour(reportHour)
            
            val valid = receipts.filter { it.paymentType != 0 }
            val cancel = receipts.filter { it.paymentType == 0 }
            val receiptCount = receipts.size
            val totalAmount = valid.sumOf { it.amountVat0 + it.amountVat1 + it.amountVat10 + it.amountVat20 }
            val canceledCount = cancel.size
            val canceledAmount = cancel.sumOf { it.amountVat0 + it.amountVat1 + it.amountVat10 + it.amountVat20 }
            val vat0 = valid.sumOf { it.amountVat0 }
            val vat1 = valid.sumOf { it.amountVat1 }
            val vat10 = valid.sumOf { it.amountVat10 }
            val vat20 = valid.sumOf { it.amountVat20 }
            val byType = valid.groupBy { it.paymentType }
                .mapValues { group -> group.value.sumOf { it.amountVat0 + it.amountVat1 + it.amountVat10 + it.amountVat20 } }
            val xml = "<testReport>" +
                    "<reportHour>$reportHour</reportHour>" +
                    "<receiptCount>$receiptCount</receiptCount>" +
                    "<totalAmount>${decimalFormat.format(totalAmount)}</totalAmount>" +
                    "<canceledInfo><count>$canceledCount</count><amount>${decimalFormat.format(canceledAmount)}</amount></canceledInfo>" +
                    "<salesVatDistribution>" +
                    "<sales><vatRate>0</vatRate><amount>${decimalFormat.format(vat0)}</amount></sales>" +
                    "<sales><vatRate>1</vatRate><amount>${decimalFormat.format(vat1)}</amount></sales>" +
                    "<sales><vatRate>10</vatRate><amount>${decimalFormat.format(vat10)}</amount></sales>" +
                    "<sales><vatRate>20</vatRate><amount>${decimalFormat.format(vat20)}</amount></sales>" +
                    "</salesVatDistribution>" +
                    "<paymentDistribution>" +
                    "<payment><paymentType>Cash</paymentType><amount>${decimalFormat.format(byType[1] ?: 0.0)}</amount></payment>" +
                    "<payment><paymentType>Credit</paymentType><amount>${decimalFormat.format(byType[2] ?: 0.0)}</amount></payment>" +
                    "<payment><paymentType>Coupon</paymentType><amount>${decimalFormat.format(byType[3] ?: 0.0)}</amount></payment>" +
                    "</paymentDistribution>" +
                    "</testReport>"
            val url = URL("http://$serverIp:4478")
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/xml; charset=UTF-8")
                outputStream.use { it.write(xml.toByteArray()) }
                Log.d("HuginDB", "Test report sent, response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("HuginDB", "Error sending test report", e)
        }
    }
}
