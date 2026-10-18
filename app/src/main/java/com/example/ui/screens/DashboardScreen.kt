package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InvoiceEntity
import com.example.data.entity.ProductEntity
import com.example.domain.model.ShopInfo
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: KiranaViewModel,
    shopInfo: ShopInfo,
    onNavigateToBilling: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToUdhaar: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToHisaab: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val customersWithUdhaar by viewModel.customersWithUdhaar.collectAsState()
    val todayHisaab by viewModel.todayHisaab.collectAsState()

    // Today's metrics calculation
    val todayStart = remember { FormatUtils.getStartOfToday() }
    val todayEnd = remember { FormatUtils.getEndOfToday() }
    val todayInvoices = remember(invoices) {
        invoices.filter { it.timestamp in todayStart..todayEnd }
    }

    val todaySales = remember(todayInvoices) { todayInvoices.sumOf { it.finalAmount } }
    val todayBillsCount = todayInvoices.size
    val todayCash = remember(todayInvoices) { todayInvoices.sumOf { it.cashPaid } }
    val todayUpi = remember(todayInvoices) { todayInvoices.sumOf { it.upiPaid } }
    val totalMarketUdhaar = remember(customersWithUdhaar) { customersWithUdhaar.sumOf { it.totalUdhaar } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PosBg)
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shop Welcome & Date Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = shopInfo.shopName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KiranaNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "दुकान डैशबोर्ड | " + FormatUtils.formatDateShort(System.currentTimeMillis()),
                            style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                        )
                    }

                    // Prominent Quick New Bill Action
                    Button(
                        onClick = onNavigateToBilling,
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("quick_new_bill_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KiranaGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "New Bill", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "नया बिल / POS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Metrics Grid (2x2 and row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "आज की बिक्री (Today's Sales)",
                        value = FormatUtils.formatRupee(todaySales),
                        subtitle = "$todayBillsCount Bills / बिल",
                        icon = Icons.Default.CurrencyRupee,
                        accentColor = KiranaGreenPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "बकाया उधार (Pending Udhaar)",
                        value = FormatUtils.formatRupee(totalMarketUdhaar),
                        subtitle = "${customersWithUdhaar.size} Customers / ग्राहक",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = UdhaarRed,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToUdhaar() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "नकद मिला (Cash In)",
                        value = FormatUtils.formatRupee(todayCash),
                        subtitle = "Today's Cash / नकद",
                        icon = Icons.Default.Payments,
                        accentColor = CashAmber,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "UPI मिला (UPI In)",
                        value = FormatUtils.formatRupee(todayUpi),
                        subtitle = "Online / डिजिटल",
                        icon = Icons.Default.QrCodeScanner,
                        accentColor = UpiBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Low Stock Alert Banner (if any)
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToStock() }
                        .testTag("low_stock_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberWarningBg),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberWarning),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "कम स्टॉक चेतावनी (Low Stock Alert)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                )
                                Text(
                                    text = "${lowStockProducts.size} सामान खत्म होने वाले हैं। ऑर्डर करें!",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF92400E))
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View Stock",
                            tint = Color(0xFF78350F)
                        )
                    }
                }
            }
        }

        // Quick Navigation Hub
        item {
            Text(
                text = "दुकान सेवाएं / Quick Actions",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = KiranaNavyText
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionTile(
                    title = "सामान (Items)",
                    icon = Icons.Default.Inventory2,
                    color = KiranaNavyHeader,
                    onClick = onNavigateToProducts,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "उधार (Udhaar)",
                    icon = Icons.Default.MenuBook,
                    color = UdhaarRed,
                    onClick = onNavigateToUdhaar,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "खरीद (Purchase)",
                    icon = Icons.Default.LocalShipping,
                    color = UpiBlue,
                    onClick = onNavigateToPurchases,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "हिसाब (Hisaab)",
                    icon = Icons.Default.Calculate,
                    color = KiranaGreenDark,
                    onClick = onNavigateToHisaab,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Invoices Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "हाल के बिल (Recent Bills)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KiranaNavyText
                    )
                )
                TextButton(onClick = onNavigateToReports) {
                    Text("सभी रिपोर्ट्स / View All", color = KiranaGreenDark, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Invoices List
        if (invoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = "No bills",
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "अभी तक कोई बिल नहीं बना है",
                            style = MaterialTheme.typography.bodyMedium.copy(color = KiranaNavySubtle)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToBilling,
                            colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
                        ) {
                            Text("पहला बिल बनाएं / Create Bill")
                        }
                    }
                }
            }
        } else {
            items(invoices.take(6)) { invoice ->
                InvoiceSummaryCard(
                    invoice = invoice,
                    onClick = { viewModel.viewInvoice(invoice.id) }
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = KiranaNavySubtle,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = KiranaNavyText
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = KiranaNavyText
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InvoiceSummaryCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KiranaGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = "Bill",
                        tint = KiranaGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KiranaNavyText
                        )
                    )
                    Text(
                        text = "${invoice.invoiceNumber} • ${FormatUtils.formatDateShort(invoice.timestamp)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatRupee(invoice.finalAmount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KiranaGreenDark
                    )
                )

                Text(
                    text = invoice.paymentMethod,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = when (invoice.paymentMethod) {
                            "CASH" -> KiranaGreenDark
                            "UPI" -> UpiBlue
                            "UDHAAR" -> UdhaarRed
                            else -> KiranaNavySubtle
                        }
                    )
                )
            }
        }
    }
}
