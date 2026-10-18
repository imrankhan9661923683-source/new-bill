package com.example.domain.model

import com.example.data.entity.InvoiceEntity
import com.example.data.entity.InvoiceItemEntity
import com.example.data.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Double = 1.0,
    val unitPrice: Double = product.sellingPrice
) {
    val lineTotal: Double get() = quantity * unitPrice
    val totalCost: Double get() = quantity * product.purchasePrice
    val taxAmount: Double get() = (lineTotal * product.taxRate) / 100.0
}

data class InvoiceDetail(
    val invoice: InvoiceEntity,
    val items: List<InvoiceItemEntity>
)

data class DailySummary(
    val dateString: String,
    val totalSales: Double = 0.0,
    val totalBills: Int = 0,
    val cashSales: Double = 0.0,
    val upiSales: Double = 0.0,
    val udhaarSales: Double = 0.0,
    val udhaarCollected: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val cogs: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val openingCash: Double = 0.0,
    val estimatedClosingCash: Double = 0.0
)

data class ShopInfo(
    val shopName: String = "श्री गणेश किराना स्टोर / Shri Ganesh Kirana Store",
    val ownerName: String = "राजेश कुमार / Rajesh Kumar",
    val phone: String = "+91 98765 43210",
    val address: String = "मेन बाजार, किराना मार्केट, भारत",
    val gstin: String = "07AAAAA0000A1Z5",
    val footerMessage: String = "धन्यवाद! फिर पधारें! / Thank you, visit again!",
    val language: String = "HINGLISH" // HINGLISH, HINDI, ENGLISH
)
