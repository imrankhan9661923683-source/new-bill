package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InvoiceEntity
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val customersWithUdhaar by viewModel.customersWithUdhaar.collectAsState()

    var timeFilter by remember { mutableStateOf(0) } // 0 = Today, 1 = This Week (7 days), 2 = This Month (30 days), 3 = All Time

    val filteredInvoices = remember(invoices, timeFilter) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (timeFilter) {
            0 -> invoices.filter { it.timestamp in FormatUtils.getStartOfToday()..FormatUtils.getEndOfToday() }
            1 -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                invoices.filter { it.timestamp >= cal.timeInMillis }
            }
            2 -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                invoices.filter { it.timestamp >= cal.timeInMillis }
            }
            else -> invoices
        }
    }

    val filteredExpenses = remember(expenses, timeFilter) {
        val cal = Calendar.getInstance()
        when (timeFilter) {
            0 -> expenses.filter { it.timestamp in FormatUtils.getStartOfToday()..FormatUtils.getEndOfToday() }
            1 -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                expenses.filter { it.timestamp >= cal.timeInMillis }
            }
            2 -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                expenses.filter { it.timestamp >= cal.timeInMillis }
            }
            else -> expenses
        }
    }

    val totalSales = remember(filteredInvoices) { filteredInvoices.sumOf { it.finalAmount } }
    val totalBills = filteredInvoices.size
    val totalCashSales = remember(filteredInvoices) { filteredInvoices.sumOf { it.cashPaid } }
    val totalUpiSales = remember(filteredInvoices) { filteredInvoices.sumOf { it.upiPaid } }
    val totalUdhaarSales = remember(filteredInvoices) { filteredInvoices.sumOf { it.udhaarAmount } }
    val totalCogs = remember(filteredInvoices) { filteredInvoices.sumOf { it.totalCostPrice } }
    val grossProfit = totalSales - totalCogs
    val totalExpensesAmount = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }
    val estimatedNetProfit = grossProfit - totalExpensesAmount
    val totalPendingUdhaar = remember(customersWithUdhaar) { customersWithUdhaar.sumOf { it.totalUdhaar } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("बिक्री व मुनाफा रिपोर्ट (Reports)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KiranaNavyText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PosBg)
                .padding(padding)
                .padding(14.dp)
                .testTag("reports_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val labels = listOf("आज (Today)", "7 दिन (Week)", "30 दिन (Month)", "कुल (All)")
                    labels.forEachIndexed { index, label ->
                        FilterChip(
                            selected = timeFilter == index,
                            onClick = { timeFilter = index },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KiranaGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Key Profit & Sales Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("कुल बिक्री (Total Sales)", fontSize = 11.sp, color = KiranaNavySubtle)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                FormatUtils.formatRupee(totalSales),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = KiranaNavyText
                            )
                            Text("$totalBills Bills", fontSize = 11.sp, color = KiranaNavySubtle)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("सकल लाभ (Gross Profit)", fontSize = 11.sp, color = KiranaNavySubtle)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                FormatUtils.formatRupee(grossProfit),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = KiranaGreenDark
                            )
                            val marginPercent = if (totalSales > 0) (grossProfit / totalSales) * 100 else 0.0
                            Text("Margin: ${marginPercent.toInt()}%", fontSize = 11.sp, color = KiranaGreenPrimary)
                        }
                    }
                }
            }

            // Net Profit Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KiranaGreenLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "शुद्ध अनुमानित मुनाफा (Net Profit)",
                                fontWeight = FontWeight.Bold,
                                color = KiranaGreenDark,
                                fontSize = 13.sp
                            )
                            Text(
                                "(सकल लाभ ₹${grossProfit.toInt()} - खर्चे ₹${totalExpensesAmount.toInt()})",
                                fontSize = 11.sp,
                                color = KiranaGreenDark
                            )
                        }

                        Text(
                            FormatUtils.formatRupee(estimatedNetProfit),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = KiranaGreenDark
                        )
                    }
                }
            }

            // Payment Methods Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "भुगतान विधि विवरण (Payment Modes)",
                            fontWeight = FontWeight.Bold,
                            color = KiranaNavyText
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        PaymentModeRow("नकद बिक्री (Cash)", totalCashSales, KiranaGreenDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentModeRow("UPI / डिजिटल", totalUpiSales, UpiBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentModeRow("उधार बिक्री (Credit)", totalUdhaarSales, UdhaarRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentModeRow("दुकान खर्चे (Expenses)", totalExpensesAmount, Color(0xFF6B7280))
                    }
                }
            }

            // Market Udhaar Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = UdhaarRedBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("मार्केट में बकाया उधार (Total Udhaar)", fontWeight = FontWeight.Bold, color = UdhaarRed)
                            Text("${customersWithUdhaar.size} ग्राहकों पर", fontSize = 11.sp, color = Color(0xFF7F1D1D))
                        }
                        Text(
                            FormatUtils.formatRupee(totalPendingUdhaar),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = UdhaarRed
                        )
                    }
                }
            }

            // Invoices in this period
            item {
                Text(
                    "बिल सूची (Invoices) - ${filteredInvoices.size} Bills",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KiranaNavyText
                    )
                )
            }

            if (filteredInvoices.isEmpty()) {
                item {
                    Text("इस अवधि में कोई बिल नहीं है", color = KiranaNavySubtle)
                }
            } else {
                items(filteredInvoices) { inv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.viewInvoice(inv.id) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inv.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "${inv.invoiceNumber} • ${FormatUtils.formatDateShort(inv.timestamp)}",
                                    fontSize = 11.sp,
                                    color = KiranaNavySubtle
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    FormatUtils.formatRupee(inv.finalAmount),
                                    fontWeight = FontWeight.Bold,
                                    color = KiranaGreenDark
                                )
                                Text(inv.paymentMethod, fontSize = 10.sp, color = KiranaNavySubtle)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentModeRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, color = KiranaNavyText)
        }
        Text(FormatUtils.formatRupee(amount), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
    }
}
