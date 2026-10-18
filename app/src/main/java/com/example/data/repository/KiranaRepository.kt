package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.database.SampleData
import com.example.data.entity.*
import com.example.domain.model.CartItem
import com.example.domain.model.DailySummary
import com.example.domain.model.InvoiceDetail
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class KiranaRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val invoiceDao = database.invoiceDao()
    private val udhaarDao = database.udhaarDao()
    private val supplierDao = database.supplierDao()
    private val purchaseDao = database.purchaseDao()
    private val stockMovementDao = database.stockMovementDao()
    private val expenseDao = database.expenseDao()
    private val dailyHisaabDao = database.dailyHisaabDao()

    // Products
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)
    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String): ProductEntity? = productDao.getProductByBarcode(barcode)
    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun adjustStock(productId: Long, delta: Double, reason: String) {
        val product = productDao.getProductById(productId) ?: return
        val newStock = (product.stockQuantity + delta).coerceAtLeast(0.0)
        productDao.updateStock(productId, newStock)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                productName = product.name,
                type = if (delta >= 0) "ADJUSTMENT_IN" else "ADJUSTMENT_OUT",
                quantityChange = delta,
                remainingStock = newStock,
                referenceId = reason
            )
        )
    }

    // Customers
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    fun getCustomersWithUdhaar(): Flow<List<CustomerEntity>> = customerDao.getCustomersWithUdhaar()
    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Invoices & Billing
    fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    suspend fun getInvoiceDetail(invoiceId: Long): InvoiceDetail? {
        val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return null
        val items = invoiceDao.getItemsForInvoice(invoiceId)
        return InvoiceDetail(invoice, items)
    }

    suspend fun createBill(
        cartItems: List<CartItem>,
        customer: CustomerEntity?,
        paymentMethod: String,
        cashPaid: Double,
        upiPaid: Double,
        udhaarAmount: Double,
        discountAmount: Double,
        notes: String = ""
    ): InvoiceEntity {
        if (cartItems.isEmpty()) throw IllegalArgumentException("Cart cannot be empty")

        return database.withTransaction {
            val subtotal = cartItems.sumOf { it.lineTotal }
            val taxAmount = cartItems.sumOf { it.taxAmount }
            val finalAmount = (subtotal - discountAmount + taxAmount).coerceAtLeast(0.0)
            val totalCostPrice = cartItems.sumOf { it.totalCost }

            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
            val invoiceNumber = "SKB-${dateFormat.format(Date(timestamp))}"

            val invoice = InvoiceEntity(
                invoiceNumber = invoiceNumber,
                timestamp = timestamp,
                customerId = customer?.id,
                customerName = customer?.name ?: "Walk-in / नकद ग्राहक",
                customerPhone = customer?.phone ?: "",
                subtotal = subtotal,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                finalAmount = finalAmount,
                totalCostPrice = totalCostPrice,
                paymentMethod = paymentMethod,
                cashPaid = cashPaid,
                upiPaid = upiPaid,
                udhaarAmount = udhaarAmount,
                notes = notes
            )

            val invoiceId = invoiceDao.insertInvoice(invoice)

            // Save invoice items and update stock
            val invoiceItems = cartItems.map { item ->
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productId = item.product.id,
                    productName = item.product.name,
                    unit = item.product.unit,
                    quantity = item.quantity,
                    purchasePrice = item.product.purchasePrice,
                    sellingPrice = item.unitPrice,
                    taxRate = item.product.taxRate,
                    lineTotal = item.lineTotal
                )
            }
            invoiceDao.insertInvoiceItems(invoiceItems)

            // Decrease stock and log movements
            for (item in cartItems) {
                val currentProduct = productDao.getProductById(item.product.id)
                if (currentProduct != null) {
                    val remaining = (currentProduct.stockQuantity - item.quantity).coerceAtLeast(0.0)
                    productDao.updateStock(currentProduct.id, remaining)
                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            productId = currentProduct.id,
                            productName = currentProduct.name,
                            type = "SALE",
                            quantityChange = -item.quantity,
                            remainingStock = remaining,
                            referenceId = invoiceNumber
                        )
                    )
                }
            }

            // Update Udhaar if applicable
            if (customer != null && udhaarAmount > 0.0) {
                val newUdhaar = customer.totalUdhaar + udhaarAmount
                customerDao.updateCustomerUdhaar(customer.id, newUdhaar)
                udhaarDao.insertTransaction(
                    UdhaarTransactionEntity(
                        customerId = customer.id,
                        customerName = customer.name,
                        type = "GIVE",
                        amount = udhaarAmount,
                        invoiceId = invoiceId,
                        paymentMethod = "BILL_CREDIT",
                        notes = "Bill: $invoiceNumber"
                    )
                )
            }

            invoice.copy(id = invoiceId)
        }
    }

    // Udhaar Khata
    fun getAllUdhaarTransactions(): Flow<List<UdhaarTransactionEntity>> = udhaarDao.getAllTransactions()
    fun getTransactionsForCustomer(customerId: Long): Flow<List<UdhaarTransactionEntity>> =
        udhaarDao.getTransactionsForCustomer(customerId)

    suspend fun recordUdhaarPayment(
        customer: CustomerEntity,
        amount: Double,
        paymentMethod: String, // CASH or UPI
        notes: String
    ) {
        if (amount <= 0.0) return
        database.withTransaction {
            val updatedUdhaar = (customer.totalUdhaar - amount).coerceAtLeast(0.0)
            customerDao.updateCustomerUdhaar(customer.id, updatedUdhaar)
            udhaarDao.insertTransaction(
                UdhaarTransactionEntity(
                    customerId = customer.id,
                    customerName = customer.name,
                    type = "TAKE",
                    amount = amount,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )
        }
    }

    suspend fun recordManualUdhaarGiven(
        customer: CustomerEntity,
        amount: Double,
        notes: String
    ) {
        if (amount <= 0.0) return
        database.withTransaction {
            val updatedUdhaar = customer.totalUdhaar + amount
            customerDao.updateCustomerUdhaar(customer.id, updatedUdhaar)
            udhaarDao.insertTransaction(
                UdhaarTransactionEntity(
                    customerId = customer.id,
                    customerName = customer.name,
                    type = "GIVE",
                    amount = amount,
                    paymentMethod = "MANUAL_CREDIT",
                    notes = notes
                )
            )
        }
    }

    // Suppliers & Purchases
    fun getAllSuppliers(): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.deleteSupplier(supplier)

    fun getAllPurchases(): Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()

    data class PurchaseItemInput(
        val product: ProductEntity,
        val quantity: Double,
        val purchasePrice: Double
    )

    suspend fun recordPurchase(
        supplier: SupplierEntity?,
        items: List<PurchaseItemInput>,
        notes: String = ""
    ): PurchaseEntity {
        if (items.isEmpty()) throw IllegalArgumentException("Purchase items cannot be empty")

        return database.withTransaction {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
            val purchaseNumber = "PUR-${dateFormat.format(Date(timestamp))}"
            val totalAmount = items.sumOf { it.quantity * it.purchasePrice }

            val purchase = PurchaseEntity(
                purchaseNumber = purchaseNumber,
                supplierId = supplier?.id,
                supplierName = supplier?.name ?: "Direct Cash Purchase",
                totalAmount = totalAmount,
                timestamp = timestamp,
                notes = notes
            )
            val purchaseId = purchaseDao.insertPurchase(purchase)

            val purchaseItems = items.map { item ->
                PurchaseItemEntity(
                    purchaseId = purchaseId,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    purchasePrice = item.purchasePrice,
                    lineTotal = item.quantity * item.purchasePrice
                )
            }
            purchaseDao.insertPurchaseItems(purchaseItems)

            // Increase stock & optionally update purchase price
            for (item in items) {
                val currentProduct = productDao.getProductById(item.product.id)
                if (currentProduct != null) {
                    val newStock = currentProduct.stockQuantity + item.quantity
                    // Update purchase price and stock
                    productDao.updateProduct(
                        currentProduct.copy(
                            stockQuantity = newStock,
                            purchasePrice = item.purchasePrice,
                            updatedAt = timestamp
                        )
                    )
                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            productId = currentProduct.id,
                            productName = currentProduct.name,
                            type = "PURCHASE",
                            quantityChange = item.quantity,
                            remainingStock = newStock,
                            referenceId = purchaseNumber
                        )
                    )
                }
            }

            purchase.copy(id = purchaseId)
        }
    }

    // Stock Movements
    fun getAllStockMovements(): Flow<List<StockMovementEntity>> = stockMovementDao.getAllMovements()

    // Expenses
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    suspend fun recordExpense(category: String, amount: Double, paymentMode: String, note: String): Long {
        return expenseDao.insertExpense(
            ExpenseEntity(
                category = category,
                amount = amount,
                paymentMode = paymentMode,
                note = note
            )
        )
    }
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    // Daily Hisaab & Reports
    fun getDailyHisaab(dateString: String): Flow<DailyHisaabEntity?> = dailyHisaabDao.getHisaabForDate(dateString)

    suspend fun setOpeningCash(dateString: String, amount: Double) {
        val existing = dailyHisaabDao.getHisaabForDateDirect(dateString)
        if (existing != null) {
            dailyHisaabDao.insertOrUpdateHisaab(existing.copy(openingCash = amount))
        } else {
            dailyHisaabDao.insertOrUpdateHisaab(
                DailyHisaabEntity(
                    dateString = dateString,
                    openingCash = amount
                )
            )
        }
    }

    suspend fun closeDailyHisaab(dateString: String, closingCash: Double, notes: String) {
        val existing = dailyHisaabDao.getHisaabForDateDirect(dateString)
        if (existing != null) {
            dailyHisaabDao.insertOrUpdateHisaab(
                existing.copy(
                    closingCash = closingCash,
                    notes = notes,
                    isClosed = true
                )
            )
        } else {
            dailyHisaabDao.insertOrUpdateHisaab(
                DailyHisaabEntity(
                    dateString = dateString,
                    closingCash = closingCash,
                    notes = notes,
                    isClosed = true
                )
            )
        }
    }

    suspend fun calculateDailySummary(startOfDay: Long, endOfDay: Long, dateString: String): DailySummary {
        val invoices = invoiceDao.getInvoicesByDateRangeDirect(startOfDay, endOfDay)
        val hisaab = dailyHisaabDao.getHisaabForDateDirect(dateString)

        val totalSales = invoices.sumOf { it.finalAmount }
        val totalBills = invoices.size
        val cashSales = invoices.sumOf { it.cashPaid }
        val upiSales = invoices.sumOf { it.upiPaid }
        val udhaarSales = invoices.sumOf { it.udhaarAmount }
        val totalCostOfGoodsSold = invoices.sumOf { it.totalCostPrice }
        val grossProfit = totalSales - totalCostOfGoodsSold

        val openingCash = hisaab?.openingCash ?: 0.0
        val estimatedClosingCash = openingCash + cashSales

        return DailySummary(
            dateString = dateString,
            totalSales = totalSales,
            totalBills = totalBills,
            cashSales = cashSales,
            upiSales = upiSales,
            udhaarSales = udhaarSales,
            cogs = totalCostOfGoodsSold,
            grossProfit = grossProfit,
            openingCash = openingCash,
            estimatedClosingCash = estimatedClosingCash
        )
    }

    suspend fun resetWithSampleData() {
        database.withTransaction {
            database.clearAllTables()
            productDao.insertAll(SampleData.initialProducts)
            customerDao.insertAll(SampleData.initialCustomers)
            supplierDao.insertAll(SampleData.initialSuppliers)
        }
    }
}
