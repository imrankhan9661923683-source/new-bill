package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR hindiName LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stockQuantity <= minStockAlert ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = :newStock, updatedAt = :timestamp WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Double, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE totalUdhaar > 0 ORDER BY totalUdhaar DESC")
    fun getCustomersWithUdhaar(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET totalUdhaar = :totalUdhaar WHERE id = :customerId")
    suspend fun updateCustomerUdhaar(customerId: Long, totalUdhaar: Double)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :number LIMIT 1")
    suspend fun getInvoiceByNumber(number: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getInvoicesByDateRange(startTime: Long, endTime: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getInvoicesByDateRangeDirect(startTime: Long, endTime: Long): List<InvoiceEntity>

    @Query("SELECT invoiceNumber FROM invoices ORDER BY id DESC LIMIT 1")
    suspend fun getLatestInvoiceNumber(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
}

@Dao
interface UdhaarDao {
    @Query("SELECT * FROM udhaar_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<UdhaarTransactionEntity>>

    @Query("SELECT * FROM udhaar_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<UdhaarTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: UdhaarTransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(tx: UdhaarTransactionEntity)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getSupplierCount(): Int
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchase(purchaseId: Long): List<PurchaseItemEntity>

    @Query("SELECT purchaseNumber FROM purchases ORDER BY id DESC LIMIT 1")
    suspend fun getLatestPurchaseNumber(): String?
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT 200")
    fun getAllMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getExpensesByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface DailyHisaabDao {
    @Query("SELECT * FROM daily_hisaab WHERE dateString = :dateString LIMIT 1")
    fun getHisaabForDate(dateString: String): Flow<DailyHisaabEntity?>

    @Query("SELECT * FROM daily_hisaab WHERE dateString = :dateString LIMIT 1")
    suspend fun getHisaabForDateDirect(dateString: String): DailyHisaabEntity?

    @Query("SELECT * FROM daily_hisaab ORDER BY timestamp DESC")
    fun getAllHisaab(): Flow<List<DailyHisaabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHisaab(hisaab: DailyHisaabEntity): Long
}
