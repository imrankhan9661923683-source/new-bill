package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ShopInfo
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.LanguageMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: KiranaViewModel,
    shopInfo: ShopInfo,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val languageMode by viewModel.languageMode.collectAsState()
    val products by viewModel.products.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var shopName by remember { mutableStateOf(shopInfo.shopName) }
    var ownerName by remember { mutableStateOf(shopInfo.ownerName) }
    var phone by remember { mutableStateOf(shopInfo.phone) }
    var address by remember { mutableStateOf(shopInfo.address) }
    var gstin by remember { mutableStateOf(shopInfo.gstin) }
    var footerMessage by remember { mutableStateOf(shopInfo.footerMessage) }

    var showResetDialog by remember { mutableStateOf(false) }

    fun shareDataBackup() {
        val backupText = StringBuilder()
        backupText.append("🏪 *SMART KIRANA BILLING DATA BACKUP*\n")
        backupText.append("Shop: ${shopInfo.shopName}\n")
        backupText.append("Phone: ${shopInfo.phone}\n")
        backupText.append("Date: ${java.util.Date()}\n")
        backupText.append("----------------------------\n")
        backupText.append("Total Products: ${products.size}\n")
        backupText.append("Total Customers: ${customers.size}\n")
        backupText.append("Total Invoices: ${invoices.size}\n")
        val totalUdhaar = customers.sumOf { it.totalUdhaar }
        backupText.append("Total Market Udhaar: ₹${totalUdhaar.toInt()}\n")
        backupText.append("----------------------------\n")
        backupText.append("Customers with Pending Udhaar:\n")
        customers.filter { it.totalUdhaar > 0 }.forEach { c ->
            backupText.append(" - ${c.name}: ₹${c.totalUdhaar.toInt()} (Ph: ${c.phone})\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, backupText.toString())
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Export Shop Data / बैकअप शेयर करें"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("सेटिंग्स (Shop Settings)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
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
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Offline & Database Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = KiranaGreenLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = KiranaGreenDark)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "100% Offline Ready & Local Persistence",
                                fontWeight = FontWeight.Bold,
                                color = KiranaGreenDark,
                                fontSize = 13.sp
                            )
                            Text(
                                "सभी बिल और हिसाब सुरक्षित स्थानीय डेटाबेस (Room SQLite) में सेव हैं।",
                                fontSize = 11.sp,
                                color = KiranaGreenDark
                            )
                        }
                    }
                }
            }

            // Language Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("भाषा चुनें / Select Language", fontWeight = FontWeight.Bold, color = KiranaNavyText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = languageMode == LanguageMode.HINGLISH,
                                onClick = { viewModel.setLanguageMode(LanguageMode.HINGLISH) },
                                label = { Text("Hinglish (हिंदी + Eng)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KiranaGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = languageMode == LanguageMode.HINDI,
                                onClick = { viewModel.setLanguageMode(LanguageMode.HINDI) },
                                label = { Text("हिंदी (Hindi)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KiranaGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Shop Details Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("दुकान का विवरण / Shop Profile", fontWeight = FontWeight.Bold, color = KiranaNavyText)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Name / दुकान का नाम *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner Name / संचालक का नाम") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number / मोबाइल नंबर") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address / दुकान का पता") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            label = { Text("GSTIN Number (वैकल्पिक / Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = footerMessage,
                            onValueChange = { footerMessage = it },
                            label = { Text("Receipt Footer Note / बिल संदेश") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateShopInfo(
                                    ShopInfo(
                                        shopName = shopName,
                                        ownerName = ownerName,
                                        phone = phone,
                                        address = address,
                                        gstin = gstin,
                                        footerMessage = footerMessage,
                                        language = languageMode.name
                                    )
                                )
                                viewModel.showMessage("Shop details saved / दुकान विवरण सुरक्षित किया गया")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Shop Details / सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Data Backup & Reset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("डेटा बैकअप व रीसेट / Backup & Reset", fontWeight = FontWeight.Bold, color = KiranaNavyText)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { shareDataBackup() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = UpiBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Shop Data (बैकअप शेयर करें)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UdhaarRed)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = UdhaarRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Sample Kirana Data / पुनः सैंपल डेटा भरें")
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("डेटा रीसेट करें? / Reset Data") },
            text = { Text("क्या आप सैंपल किराने के सामान, ग्राहक व सप्लायर रीस्टोर करना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToSampleData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
