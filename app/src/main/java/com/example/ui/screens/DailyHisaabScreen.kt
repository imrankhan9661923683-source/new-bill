package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseEntity
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHisaabScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit
) {
    val todayHisaab by viewModel.todayHisaab.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val udhaarTransactions by viewModel.udhaarTransactions.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val todayStart = remember { FormatUtils.getStartOfToday() }
    val todayEnd = remember { FormatUtils.getEndOfToday() }

    // Today's collections
    val todayInvoices = remember(invoices) { invoices.filter { it.timestamp in todayStart..todayEnd } }
    val cashSales = remember(todayInvoices) { todayInvoices.sumOf { it.cashPaid } }
    val upiSales = remember(todayInvoices) { todayInvoices.sumOf { it.upiPaid } }

    val todayUdhaarCollections = remember(udhaarTransactions) {
        udhaarTransactions.filter { it.type == "TAKE" && it.timestamp in todayStart..todayEnd }
    }
    val cashUdhaarCollected = remember(todayUdhaarCollections) {
        todayUdhaarCollections.filter { it.paymentMethod == "CASH" }.sumOf { it.amount }
    }
    val upiUdhaarCollected = remember(todayUdhaarCollections) {
        todayUdhaarCollections.filter { it.paymentMethod == "UPI" }.sumOf { it.amount }
    }

    val todayExpenses = remember(expenses) { expenses.filter { it.timestamp in todayStart..todayEnd } }
    val totalCashExpenses = remember(todayExpenses) {
        todayExpenses.filter { it.paymentMode == "CASH" }.sumOf { it.amount }
    }
    val totalUpiExpenses = remember(todayExpenses) {
        todayExpenses.filter { it.paymentMode == "UPI" }.sumOf { it.amount }
    }

    val openingCash = todayHisaab?.openingCash ?: 0.0
    val totalCashIn = cashSales + cashUdhaarCollected
    val totalCashOut = totalCashExpenses
    val expectedClosingCash = (openingCash + totalCashIn - totalCashOut).coerceAtLeast(0.0)

    var showOpeningCashDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showClosingCashDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("दैनिक हिसाब (Daily Hisaab)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
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
                .testTag("daily_hisaab_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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
                        Column {
                            Text(
                                text = "आज का गल्ला व रोकड़ (Cash Register)",
                                fontWeight = FontWeight.Bold,
                                color = KiranaNavyText
                            )
                            Text(
                                text = FormatUtils.formatDate(System.currentTimeMillis()),
                                fontSize = 12.sp,
                                color = KiranaNavySubtle
                            )
                        }

                        if (todayHisaab?.isClosed == true) {
                            Surface(color = KiranaGreenLight, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "हिसाब बंद (Closed)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KiranaGreenDark
                                )
                            }
                        }
                    }
                }
            }

            // Opening Cash Tile
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOpeningCashDialog = true },
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("सुबह का गल्ला (Opening Cash)", fontWeight = FontWeight.Bold, color = KiranaNavyText)
                                Text("Tap to set / बदलें", fontSize = 11.sp, color = KiranaNavySubtle)
                            }
                        }

                        Text(
                            FormatUtils.formatRupee(openingCash),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = KiranaNavyText
                        )
                    }
                }
            }

            // Cash Inflow Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = JamaGreenBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "आज की कुल नकद आवक (+ Cash Inflow)",
                            fontWeight = FontWeight.Bold,
                            color = JamaGreen,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HisaabLineItem("नकद बिक्री (Cash Sales)", FormatUtils.formatRupee(cashSales))
                        HisaabLineItem("उधार वसूली (Udhaar Collected)", FormatUtils.formatRupee(cashUdhaarCollected))
                        HorizontalDivider(color = Color(0xFFA7F3D0), modifier = Modifier.padding(vertical = 4.dp))
                        HisaabLineItem("कुल नकद आया (Total Cash In)", FormatUtils.formatRupee(totalCashIn), isBold = true)
                    }
                }
            }

            // UPI Inflow Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = UpiBlueBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "आज की UPI आवक (+ Online / Bank)",
                            fontWeight = FontWeight.Bold,
                            color = UpiBlue,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HisaabLineItem("UPI बिक्री (UPI Sales)", FormatUtils.formatRupee(upiSales))
                        HisaabLineItem("UPI उधार वसूली", FormatUtils.formatRupee(upiUdhaarCollected))
                        HorizontalDivider(color = Color(0xFFBFDBFE), modifier = Modifier.padding(vertical = 4.dp))
                        HisaabLineItem("कुल UPI (Total Digital)", FormatUtils.formatRupee(upiSales + upiUdhaarCollected), isBold = true)
                    }
                }
            }

            // Cash Outflow / Expenses Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = UdhaarRedBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "दुकान खर्चे (- Expenses)",
                                fontWeight = FontWeight.Bold,
                                color = UdhaarRed,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = { showExpenseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ खर्चा जोड़ें", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        if (todayExpenses.isEmpty()) {
                            Text("आज कोई खर्चा दर्ज नहीं है", fontSize = 11.sp, color = KiranaNavySubtle)
                        } else {
                            todayExpenses.forEach { exp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${exp.category} (${exp.paymentMode})", fontSize = 12.sp)
                                    Text("-₹${exp.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = UdhaarRed)
                                }
                            }
                            HorizontalDivider(color = Color(0xFFFECACA), modifier = Modifier.padding(vertical = 4.dp))
                            HisaabLineItem("कुल नकद खर्चा", "-${FormatUtils.formatRupee(totalCashExpenses)}", isBold = true)
                        }
                    }
                }
            }

            // Expected Cash Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "गल्ले में अपेक्षित नकद (Expected Closing Cash)",
                            fontWeight = FontWeight.Bold,
                            color = KiranaNavyText
                        )
                        Text(
                            "(सुबह का गल्ला + नकद बिक्री + वसूली - नकद खर्चा)",
                            fontSize = 11.sp,
                            color = KiranaNavySubtle
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            FormatUtils.formatRupee(expectedClosingCash),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = KiranaGreenDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (todayHisaab?.isClosed == true) {
                            Text(
                                "दर्ज शाम का गल्ला: ${FormatUtils.formatRupee(todayHisaab!!.closingCash)}",
                                fontWeight = FontWeight.Bold,
                                color = KiranaNavyText
                            )
                            val diff = todayHisaab!!.closingCash - expectedClosingCash
                            val diffText = if (diff >= 0) "+₹${diff.toInt()} (ज्यादा)" else "-₹${Math.abs(diff).toInt()} (कम / Short)"
                            Text(
                                "अंतर (Difference): $diffText",
                                fontWeight = FontWeight.Bold,
                                color = if (diff == 0.0) KiranaGreenDark else UdhaarRed
                            )
                        } else {
                            Button(
                                onClick = { showClosingCashDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("close_hisaab_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("आज का हिसाब बंद करें / Close Cash Drawer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Set Opening Cash Dialog
    if (showOpeningCashDialog) {
        SetOpeningCashDialog(
            current = openingCash,
            onDismiss = { showOpeningCashDialog = false },
            onConfirm = {
                viewModel.setOpeningCash(it)
                showOpeningCashDialog = false
            }
        )
    }

    // Add Expense Dialog
    if (showExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onConfirm = { cat, amt, mode, note ->
                viewModel.recordExpense(cat, amt, mode, note)
                showExpenseDialog = false
            }
        )
    }

    // Close Hisaab Dialog
    if (showClosingCashDialog) {
        CloseHisaabDialog(
            expected = expectedClosingCash,
            onDismiss = { showClosingCashDialog = false },
            onConfirm = { actualCash, notes ->
                viewModel.closeDailyHisaab(actualCash, notes)
                showClosingCashDialog = false
            }
        )
    }
}

@Composable
private fun HisaabLineItem(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = KiranaNavyText
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = KiranaNavyText
        )
    }
}

@Composable
private fun SetOpeningCashDialog(
    current: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(if (current > 0) current.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("सुबह का गल्ला दर्ज करें / Opening Cash") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Cash Amount (₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Set Cash")
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
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (cat: String, amt: Double, mode: String, note: String) -> Unit
) {
    var category by remember { mutableStateOf("चाय-नाश्ता / Tea & Snacks") }
    var amountText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("CASH") }
    var note by remember { mutableStateOf("") }

    val presetCategories = listOf(
        "चाय-नाश्ता / Tea & Snacks",
        "दुकान भाड़ा / Transport",
        "बिजली बिल / Electricity",
        "दुकान किराया / Rent",
        "सफाई व मजदूरी / Labour",
        "अन्य खर्च / Misc"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("खर्चा जोड़ें / Add Shop Expense") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("खर्चे का प्रकार / Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                presetCategories.take(3).forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { category = cat }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = category == cat, onClick = { category = cat })
                        Text(cat, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == "CASH",
                        onClick = { mode = "CASH" },
                        label = { Text("नकद (Cash)") }
                    )
                    FilterChip(
                        selected = mode == "UPI",
                        onClick = { mode = "UPI" },
                        label = { Text("UPI / Online") }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / विवरण") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(category, amt, mode, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
            ) {
                Text("Save Expense")
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
private fun CloseHisaabDialog(
    expected: Double,
    onDismiss: () -> Unit,
    onConfirm: (actualCash: Double, notes: String) -> Unit
) {
    var actualCashText by remember { mutableStateOf(expected.toInt().toString()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("आज का हिसाब बंद करें / Day Close") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("अपेक्षित नकद (Expected): ${FormatUtils.formatRupee(expected)}", fontWeight = FontWeight.Bold, color = KiranaGreenDark)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = actualCashText,
                    onValueChange = { actualCashText = it },
                    label = { Text("रात को गिना गया नकद (Physical Cash) ₹ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                val actual = actualCashText.toDoubleOrNull() ?: 0.0
                val diff = actual - expected
                Text(
                    text = if (diff == 0.0) "✅ हिसाब बिल्कुल बराबर है!" else if (diff > 0) "+₹${diff.toInt()} ज्यादा है" else "-₹${Math.abs(diff).toInt()} कम है",
                    color = if (diff == 0.0) KiranaGreenDark else UdhaarRed,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("दिन के अंत की टिप्पणी / Closing Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val actual = actualCashText.toDoubleOrNull() ?: 0.0
                    onConfirm(actual, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Confirm Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
