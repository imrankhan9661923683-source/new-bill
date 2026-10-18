package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"]), Index(value = ["name"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val hindiName: String = "",
    val barcode: String = "",
    val category: String = "General / सामान्य",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val unit: String = "packet",
    val stockQuantity: Double = 0.0,
    val minStockAlert: Double = 5.0,
    val taxRate: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    indices = [Index(value = ["phone"])]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val creditLimit: Double = 10000.0,
    val totalUdhaar: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNumber"], unique = true), Index(value = ["customerId"])]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "Walk-in / नकद ग्राहक",
    val customerPhone: String = "",
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val totalCostPrice: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, UPI, UDHAAR, SPLIT
    val cashPaid: Double = 0.0,
    val upiPaid: Double = 0.0,
    val udhaarAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "invoice_items",
    indices = [Index(value = ["invoiceId"]), Index(value = ["productId"])]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val quantity: Double,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val taxRate: Double,
    val lineTotal: Double
)

@Entity(
    tableName = "udhaar_transactions",
    indices = [Index(value = ["customerId"]), Index(value = ["timestamp"])]
)
data class UdhaarTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val type: String, // "GIVE" (उधार दिया / Credit given) or "TAKE" (जमा लिया / Received repayment)
    val amount: Double,
    val invoiceId: Long? = null,
    val paymentMethod: String = "CASH", // CASH, UPI, BILL
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val company: String = "",
    val address: String = ""
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseNumber: String,
    val supplierId: Long? = null,
    val supplierName: String = "",
    val totalAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    indices = [Index(value = ["purchaseId"]), Index(value = ["productId"])]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val purchasePrice: Double,
    val lineTotal: Double
)

@Entity(
    tableName = "stock_movements",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // SALE, PURCHASE, ADJUSTMENT
    val quantityChange: Double,
    val remainingStock: Double,
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["timestamp"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, UPI
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_hisaab",
    indices = [Index(value = ["dateString"], unique = true)]
)
data class DailyHisaabEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val openingCash: Double = 0.0,
    val closingCash: Double = 0.0,
    val notes: String = "",
    val isClosed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
