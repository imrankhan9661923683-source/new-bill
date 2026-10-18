package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.InvoiceDetail
import com.example.domain.model.ShopInfo
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@Composable
fun InvoiceReceiptDialog(
    detail: InvoiceDetail,
    shopInfo: ShopInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val invoice = detail.invoice
    val items = detail.items

    fun shareOnWhatsApp() {
        val sb = StringBuilder()
        sb.append("🧾 *${shopInfo.shopName}*\n")
        sb.append("📍 ${shopInfo.address}\n")
        sb.append("📞 ${shopInfo.phone}\n")
        if (shopInfo.gstin.isNotEmpty()) {
            sb.append("GSTIN: ${shopInfo.gstin}\n")
        }
        sb.append("-----------------------------\n")
        sb.append("Bill No: *${invoice.invoiceNumber}*\n")
        sb.append("Date: ${FormatUtils.formatDate(invoice.timestamp)}\n")
        sb.append("Customer: ${invoice.customerName}\n")
        if (invoice.customerPhone.isNotEmpty()) {
            sb.append("Phone: ${invoice.customerPhone}\n")
        }
        sb.append("-----------------------------\n")
        sb.append("*ITEMS:*\n")
        items.forEachIndexed { index, item ->
            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            sb.append("${index + 1}. ${item.productName}\n")
            sb.append("   ${qtyStr} ${item.unit} x ₹${item.sellingPrice} = *₹${item.lineTotal.toInt()}*\n")
        }
        sb.append("-----------------------------\n")
        sb.append("Subtotal: ₹${invoice.subtotal.toInt()}\n")
        if (invoice.discountAmount > 0) {
            sb.append("Discount: -₹${invoice.discountAmount.toInt()}\n")
        }
        if (invoice.taxAmount > 0) {
            sb.append("GST/Tax: +₹${invoice.taxAmount.toInt()}\n")
        }
        sb.append("*TOTAL AMOUNT: ₹${invoice.finalAmount.toInt()}*\n")
        sb.append("Payment Mode: *${invoice.paymentMethod}*\n")
        if (invoice.paymentMethod == "SPLIT") {
            sb.append(" - Cash: ₹${invoice.cashPaid.toInt()}\n")
            sb.append(" - UPI: ₹${invoice.upiPaid.toInt()}\n")
            sb.append(" - Udhaar: ₹${invoice.udhaarAmount.toInt()}\n")
        }
        sb.append("-----------------------------\n")
        sb.append("${shopInfo.footerMessage}\n")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Bill Receipt / बिल शेयर करें")
        context.startActivity(shareIntent)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .testTag("invoice_receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt / पक्का बिल",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KiranaNavyText
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = KiranaNavySubtle)
                    }
                }

                HorizontalDivider(color = DividerColor)

                // Printable Thermal Receipt Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .border(1.dp, DividerColor, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shop Header
                        Text(
                            text = shopInfo.shopName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KiranaGreenDark,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = shopInfo.address,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KiranaNavySubtle,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = "Phone: ${shopInfo.phone}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KiranaNavySubtle
                            )
                        )
                        if (shopInfo.gstin.isNotBlank()) {
                            Text(
                                text = "GSTIN: ${shopInfo.gstin}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = KiranaNavyText
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        // Meta row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bill: ${invoice.invoiceNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = FormatUtils.formatDateShort(invoice.timestamp),
                                style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Customer: ${invoice.customerName}",
                                style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavyText)
                            )
                            if (invoice.customerPhone.isNotEmpty()) {
                                Text(
                                    text = invoice.customerPhone,
                                    style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                                )
                            }
                        }

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        // Items Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Item / विवरण",
                                modifier = Modifier.weight(1.8f),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Qty",
                                modifier = Modifier.weight(0.7f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Rate",
                                modifier = Modifier.weight(0.9f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Amount",
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        HorizontalDivider(color = DividerColor)

                        // Items list
                        items.forEach { item ->
                            val qtyFormatted = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.productName,
                                    modifier = Modifier.weight(1.8f),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = KiranaNavyText,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "$qtyFormatted ${item.unit}",
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                                )
                                Text(
                                    text = "₹${item.sellingPrice.toInt()}",
                                    modifier = Modifier.weight(0.9f),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                                )
                                Text(
                                    text = "₹${item.lineTotal.toInt()}",
                                    modifier = Modifier.weight(1.1f),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KiranaNavyText
                                    )
                                )
                            }
                        }

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        // Calculation Rows
                        CalculationRow(label = "Subtotal (योग)", value = FormatUtils.formatRupee(invoice.subtotal))

                        if (invoice.discountAmount > 0.0) {
                            CalculationRow(
                                label = "Discount (छूट)",
                                value = "-${FormatUtils.formatRupee(invoice.discountAmount)}",
                                isDiscount = true
                            )
                        }

                        if (invoice.taxAmount > 0.0) {
                            CalculationRow(
                                label = "Tax/GST (कर)",
                                value = "+${FormatUtils.formatRupee(invoice.taxAmount)}"
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Grand Total Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KiranaGreenLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL AMOUNT:",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KiranaGreenDark
                                )
                            )
                            Text(
                                text = FormatUtils.formatRupee(invoice.finalAmount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KiranaGreenDark
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Payment Mode Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paid via / भुगतान:",
                                style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                            )
                            Text(
                                text = invoice.paymentMethod,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (invoice.paymentMethod) {
                                        "CASH" -> KiranaGreenDark
                                        "UPI" -> UpiBlue
                                        "UDHAAR" -> UdhaarRed
                                        else -> KiranaNavyText
                                    }
                                )
                            )
                        }

                        if (invoice.paymentMethod == "SPLIT") {
                            Text(
                                text = "Cash: ₹${invoice.cashPaid.toInt()} | UPI: ₹${invoice.upiPaid.toInt()} | Udhaar: ₹${invoice.udhaarAmount.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = shopInfo.footerMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = KiranaNavySubtle,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = "Generated by Smart Kirana Billing POS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                // Action Buttons (WhatsApp Share & Done)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { shareOnWhatsApp() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_whatsapp_receipt_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp / शेयर", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("done_receipt_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KiranaNavyHeader,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Done / पूर्ण", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationRow(
    label: String,
    value: String,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isDiscount) UdhaarRed else KiranaNavyText
            )
        )
    }
}
