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
import com.example.data.entity.StockMovementEntity
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInventoryScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.stockMovements.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Low Stock, 1 = All Stock, 2 = Movement History
    var adjustingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    // Valuation calculations
    val totalCostValue = remember(products) { products.sumOf { it.stockQuantity * it.purchasePrice } }
    val totalSaleValue = remember(products) { products.sumOf { it.stockQuantity * it.sellingPrice } }
    val potentialProfit = totalSaleValue - totalCostValue

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("स्टॉक व इन्वेंट्री (Stock)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
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
            // Valuation Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("कुल स्टॉक मूल्य (Cost)", fontSize = 11.sp, color = KiranaNavySubtle)
                        Text(FormatUtils.formatRupee(totalCostValue), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KiranaNavyText)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("बिक्री मूल्य (Retail)", fontSize = 11.sp, color = KiranaNavySubtle)
                        Text(FormatUtils.formatRupee(totalSaleValue), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KiranaGreenDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = SurfaceWhite,
                contentColor = KiranaGreenPrimary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            "कम स्टॉक (${lowStockProducts.size})",
                            color = if (lowStockProducts.isNotEmpty()) UdhaarRed else KiranaNavyText,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            "सभी सामान (${products.size})",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Text(
                            "मूवमेंट इतिहास",
                            fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeTab) {
                0 -> {
                    // Low Stock Tab
                    if (lowStockProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("सभी सामान पर्याप्त मात्रा में उपलब्ध हैं! / Stock is healthy", color = KiranaGreenDark)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lowStockProducts) { product ->
                                StockItemCard(
                                    product = product,
                                    onAdjust = { adjustingProduct = product }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // All Stock Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            StockItemCard(
                                product = product,
                                onAdjust = { adjustingProduct = product }
                            )
                        }
                    }
                }
                2 -> {
                    // Movement History Tab
                    if (movements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("कोई स्टॉक मूवमेंट रिकॉर्ड नहीं है", color = KiranaNavySubtle)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(movements) { mov ->
                                MovementCard(mov)
                            }
                        }
                    }
                }
            }
        }
    }

    // Stock Adjustment Dialog
    if (adjustingProduct != null) {
        StockAdjustmentDialog(
            product = adjustingProduct!!,
            onDismiss = { adjustingProduct = null },
            onConfirm = { delta, reason ->
                viewModel.adjustStock(adjustingProduct!!.id, delta, reason)
                adjustingProduct = null
            }
        )
    }
}

@Composable
private fun StockItemCard(
    product: ProductEntity,
    onAdjust: () -> Unit
) {
    val isLow = product.stockQuantity <= product.minStockAlert

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_item_${product.id}"),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = KiranaNavyText)
                if (product.hindiName.isNotBlank()) {
                    Text(product.hindiName, style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle))
                }
                Text(
                    text = "Alert threshold: ${product.minStockAlert.toInt()} ${product.unit}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    val stockFormatted = if (product.stockQuantity % 1.0 == 0.0) product.stockQuantity.toInt().toString() else product.stockQuantity.toString()
                    Text(
                        text = "$stockFormatted ${product.unit}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isLow) UdhaarRed else KiranaGreenDark
                    )
                    Text(
                        text = if (isLow) "Low Stock / कम" else "In Stock",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLow) UdhaarRed else KiranaGreenPrimary
                    )
                }

                OutlinedButton(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Adjust", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MovementCard(mov: StockMovementEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(mov.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "${mov.type} • ${mov.referenceId} • ${FormatUtils.formatDateShort(mov.timestamp)}",
                    fontSize = 11.sp,
                    color = KiranaNavySubtle
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val isPositive = mov.quantityChange > 0
                Text(
                    text = if (isPositive) "+${mov.quantityChange}" else "${mov.quantityChange}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPositive) KiranaGreenDark else UdhaarRed
                )
                Text("Remaining: ${mov.remainingStock}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (delta: Double, reason: String) -> Unit
) {
    var deltaText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("गिनती संशोधन / Physical Count") }
    var isAddition by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("स्टॉक बदलें / Adjust Stock") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("Current Stock: ${product.stockQuantity} ${product.unit}", color = KiranaNavySubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isAddition,
                        onClick = { isAddition = true },
                        label = { Text("+ स्टॉक जोड़ें / Add") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KiranaGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isAddition,
                        onClick = { isAddition = false },
                        label = { Text("- स्टॉक घटाएं / Reduce") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UdhaarRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text("Quantity (${product.unit})") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / कारण (उदा. खराब, गिनती सुधार)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = deltaText.toDoubleOrNull() ?: 0.0
                    if (qty > 0) {
                        val finalDelta = if (isAddition) qty else -qty
                        onConfirm(finalDelta, reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Update Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
