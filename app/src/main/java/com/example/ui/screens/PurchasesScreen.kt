package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.SupplierEntity
import com.example.data.repository.KiranaRepository
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit
) {
    val purchases by viewModel.purchases.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()

    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("खरीद प्रबंधन (Purchases)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KiranaNavyText)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSupplierDialog = true }) {
                        Icon(Icons.Default.DomainAdd, contentDescription = "Add Supplier", tint = KiranaGreenDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewPurchaseDialog = true },
                containerColor = KiranaGreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                text = { Text("नई खरीद / New Purchase", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("record_purchase_fab")
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
            // Summary Banner
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
                        Text("कुल दर्ज खरीद / Total Purchases", fontSize = 12.sp, color = KiranaNavySubtle)
                        val totalPurchasesAmount = remember(purchases) { purchases.sumOf { it.totalAmount } }
                        Text(
                            FormatUtils.formatRupee(totalPurchasesAmount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = KiranaNavyText
                        )
                    }

                    Button(
                        onClick = { showAddSupplierDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KiranaNavyHeader),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ सप्लायर जोड़ें", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "खरीद इतिहास / Purchase Entries (${purchases.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = KiranaNavyText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (purchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("अभी कोई खरीद दर्ज नहीं है / No purchases recorded", color = KiranaNavySubtle)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(purchases) { purchase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(purchase.supplierName, fontWeight = FontWeight.Bold, color = KiranaNavyText)
                                    Text(
                                        "${purchase.purchaseNumber} • ${FormatUtils.formatDateShort(purchase.timestamp)}",
                                        fontSize = 11.sp,
                                        color = KiranaNavySubtle
                                    )
                                    if (purchase.notes.isNotBlank()) {
                                        Text(purchase.notes, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Text(
                                    FormatUtils.formatRupee(purchase.totalAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = KiranaNavyText
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Purchase Dialog
    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            suppliers = suppliers,
            products = products,
            onDismiss = { showNewPurchaseDialog = false },
            onConfirm = { supplier, items, notes ->
                viewModel.recordPurchase(supplier, items, notes)
                showNewPurchaseDialog = false
            }
        )
    }

    // Add Supplier Dialog
    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, phone, company, address ->
                viewModel.addSupplier(name, phone, company, address)
                showAddSupplierDialog = false
            }
        )
    }
}

@Composable
private fun NewPurchaseDialog(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (supplier: SupplierEntity?, items: List<KiranaRepository.PurchaseItemInput>, notes: String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf<SupplierEntity?>(suppliers.firstOrNull()) }
    var notes by remember { mutableStateOf("") }
    val purchaseItems = remember { mutableStateListOf<KiranaRepository.PurchaseItemInput>() }

    var selectedProductToAdd by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
    var qtyText by remember { mutableStateOf("10") }
    var priceText by remember { mutableStateOf(products.firstOrNull()?.purchasePrice?.toInt()?.toString() ?: "100") }

    val totalAmount = remember(purchaseItems.size, purchaseItems.toList()) {
        purchaseItems.sumOf { it.quantity * it.purchasePrice }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("नई खरीद दर्ज करें / Record Purchase") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                Text("सप्लायर चुनें / Supplier:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (suppliers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suppliers.take(3).forEach { sup ->
                            FilterChip(
                                selected = selectedSupplier?.id == sup.id,
                                onClick = { selectedSupplier = sup },
                                label = { Text(sup.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Item Entry Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PosBg)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("सामान जोड़ें / Add Item to Purchase:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        // Simple product picker
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            products.take(4).forEach { p ->
                                FilterChip(
                                    selected = selectedProductToAdd?.id == p.id,
                                    onClick = {
                                        selectedProductToAdd = p
                                        priceText = p.purchasePrice.toInt().toString()
                                    },
                                    label = { Text(p.name.take(10), fontSize = 10.sp) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = qtyText,
                                onValueChange = { qtyText = it },
                                label = { Text("Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Cost ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val q = qtyText.toDoubleOrNull() ?: 1.0
                                    val p = priceText.toDoubleOrNull() ?: 0.0
                                    if (selectedProductToAdd != null && q > 0 && p > 0) {
                                        purchaseItems.add(
                                            KiranaRepository.PurchaseItemInput(
                                                product = selectedProductToAdd!!,
                                                quantity = q,
                                                purchasePrice = p
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterVertically),
                                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
                            ) {
                                Text("+ Add")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of items added
                Text("खरीदे गए सामान (${purchaseItems.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(purchaseItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${item.product.name} (${item.quantity} ${item.product.unit} @ ₹${item.purchasePrice.toInt()})",
                                fontSize = 11.sp
                            )
                            Text("₹${(item.quantity * item.purchasePrice).toInt()}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Total Purchase: ${FormatUtils.formatRupee(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = KiranaGreenDark,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (purchaseItems.isNotEmpty()) {
                        onConfirm(selectedSupplier, purchaseItems.toList(), notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Save & Add Stock")
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
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, company: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("नया सप्लायर / Add Supplier") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Person Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company / मंडी फर्म का नाम") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, phone, company, address)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Save Supplier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
