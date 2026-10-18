package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.UdhaarTransactionEntity
import com.example.domain.model.ShopInfo
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdhaarKhataScreen(
    viewModel: KiranaViewModel,
    shopInfo: ShopInfo,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customersWithUdhaar by viewModel.customersWithUdhaar.collectAsState()
    val allCustomers by viewModel.customers.collectAsState()
    val allTransactions by viewModel.udhaarTransactions.collectAsState()

    var selectedCustomerForLedger by remember { mutableStateOf<CustomerEntity?>(null) }
    var showGiveUdhaarDialog by remember { mutableStateOf(false) }
    var showReceivePaymentDialog by remember { mutableStateOf(false) }
    var activeCustomerForAction by remember { mutableStateOf<CustomerEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val totalUdhaar = remember(customersWithUdhaar) { customersWithUdhaar.sumOf { it.totalUdhaar } }

    val filteredCustomers = remember(allCustomers, searchQuery) {
        allCustomers.filter {
            (searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.phone.contains(searchQuery))
        }.sortedByDescending { it.totalUdhaar }
    }

    fun sendWhatsAppReminder(customer: CustomerEntity) {
        val message = "नमस्ते ${customer.name} जी 🙏,\n\n" +
                "आपकी *${shopInfo.shopName}* पर बकाया राशि (Pending Udhaar) *${FormatUtils.formatRupee(customer.totalUdhaar)}* है।\n" +
                "कृपया समय पर भुगतान करने की कृपा करें।\n\n" +
                "धन्यवाद!\n" +
                "${shopInfo.shopName}\n" +
                "📞 ${shopInfo.phone}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Send Payment Reminder / तगादा भेजें")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("उधार खाता (Udhaar Khata)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KiranaNavyText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PosBg)
                .padding(padding)
                .padding(14.dp)
        ) {
            // Market Udhaar Total Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = UdhaarRedBg),
                border = CardDefaults.outlinedCardBorder()
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
                            "कुल बकाया उधार (Total Market Credit)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            FormatUtils.formatRupee(totalUdhaar),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = UdhaarRed
                            )
                        )
                        Text(
                            "${customersWithUdhaar.size} ग्राहकों पर बाकी / Pending customers",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF991B1B))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(UdhaarRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("udhaar_search_input"),
                placeholder = { Text("Search customer by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KiranaNavySubtle) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Udhaar Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCustomers) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCustomerForLedger = customer }
                            .testTag("udhaar_customer_${customer.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (customer.totalUdhaar > 0) UdhaarRedBg else JamaGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (customer.totalUdhaar > 0) UdhaarRed else JamaGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, color = KiranaNavyText)
                                        if (customer.phone.isNotEmpty()) {
                                            Text(customer.phone, fontSize = 12.sp, color = KiranaNavySubtle)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatRupee(customer.totalUdhaar),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (customer.totalUdhaar > 0) UdhaarRed else JamaGreen
                                    )
                                    Text(
                                        text = if (customer.totalUdhaar > 0) "बाकी है (Due)" else "खाता साफ (Nil)",
                                        fontSize = 11.sp,
                                        color = if (customer.totalUdhaar > 0) UdhaarRed else JamaGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        activeCustomerForAction = customer
                                        showReceivePaymentDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("जमा लिया (Got ₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        activeCustomerForAction = customer
                                        showGiveUdhaarDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("उधार दिया (Gave ₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (customer.totalUdhaar > 0) {
                                    IconButton(
                                        onClick = { sendWhatsAppReminder(customer) },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Send Reminder",
                                            tint = Color(0xFF25D366)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Detailed Ledger Dialog
    if (selectedCustomerForLedger != null) {
        val cust = selectedCustomerForLedger!!
        val customerTransactions = remember(allTransactions, cust) {
            allTransactions.filter { it.customerId == cust.id }
        }

        CustomerLedgerDialog(
            customer = cust,
            transactions = customerTransactions,
            onDismiss = { selectedCustomerForLedger = null },
            onSendReminder = { sendWhatsAppReminder(cust) }
        )
    }

    // Receive Payment Dialog
    if (showReceivePaymentDialog && activeCustomerForAction != null) {
        ReceivePaymentDialog(
            customer = activeCustomerForAction!!,
            onDismiss = {
                showReceivePaymentDialog = false
                activeCustomerForAction = null
            },
            onConfirm = { amount, method, notes ->
                viewModel.recordUdhaarRepayment(activeCustomerForAction!!, amount, method, notes)
                showReceivePaymentDialog = false
                activeCustomerForAction = null
            }
        )
    }

    // Give Credit Dialog
    if (showGiveUdhaarDialog && activeCustomerForAction != null) {
        GiveCreditDialog(
            customer = activeCustomerForAction!!,
            onDismiss = {
                showGiveUdhaarDialog = false
                activeCustomerForAction = null
            },
            onConfirm = { amount, notes ->
                viewModel.recordManualUdhaarGiven(activeCustomerForAction!!, amount, notes)
                showGiveUdhaarDialog = false
                activeCustomerForAction = null
            }
        )
    }
}

@Composable
private fun CustomerLedgerDialog(
    customer: CustomerEntity,
    transactions: List<UdhaarTransactionEntity>,
    onDismiss: () -> Unit,
    onSendReminder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(customer.name, fontWeight = FontWeight.Bold)
                Text(
                    "बकाया उधार: ${FormatUtils.formatRupee(customer.totalUdhaar)}",
                    fontWeight = FontWeight.Bold,
                    color = if (customer.totalUdhaar > 0) UdhaarRed else JamaGreen,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("लेन-देन इतिहास / Ledger Entries:", fontSize = 12.sp, color = KiranaNavySubtle)
                Spacer(modifier = Modifier.height(6.dp))

                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text("कोई पूर्व लेन-देन दर्ज नहीं है")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(transactions) { tx ->
                            val isGiven = tx.type == "GIVE"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isGiven) UdhaarRedBg else JamaGreenBg
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isGiven) "उधार दिया (Gave)" else "जमा लिया (Received)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isGiven) UdhaarRed else JamaGreen
                                        )
                                        Text(
                                            text = "${FormatUtils.formatDateShort(tx.timestamp)} • ${tx.paymentMethod}",
                                            fontSize = 10.sp,
                                            color = KiranaNavySubtle
                                        )
                                        if (tx.notes.isNotBlank()) {
                                            Text(tx.notes, fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    Text(
                                        text = if (isGiven) "+₹${tx.amount.toInt()}" else "-₹${tx.amount.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (isGiven) UdhaarRed else JamaGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (customer.totalUdhaar > 0) {
                Button(
                    onClick = onSendReminder,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp Reminder")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close / बंद करें")
            }
        }
    )
}

@Composable
private fun ReceivePaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paymentMethod: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (customer.totalUdhaar > 0) customer.totalUdhaar.toInt().toString() else "") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("उधार चुकता / Repayment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("जमा लें (Receive Payment)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(customer.name, fontWeight = FontWeight.Bold)
                Text("वर्तमान उधार: ${FormatUtils.formatRupee(customer.totalUdhaar)}", color = UdhaarRed)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("जमा राशि (Amount) ₹ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "CASH",
                        onClick = { paymentMethod = "CASH" },
                        label = { Text("नकद (Cash)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KiranaGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = paymentMethod == "UPI",
                        onClick = { paymentMethod = "UPI" },
                        label = { Text("UPI / ऑनलाइन") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UpiBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / विवरण") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, paymentMethod, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Save Payment / जमा करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun GiveCreditDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("सौदा उधार / Manual Credit") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("उधार दें (Give Credit)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(customer.name, fontWeight = FontWeight.Bold)
                Text("वर्तमान उधार: ${FormatUtils.formatRupee(customer.totalUdhaar)}", color = UdhaarRed)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("उधार राशि (Credit Amount) ₹ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / सामान का नाम") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
            ) {
                Text("उधार चढ़ाएं / Give")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
