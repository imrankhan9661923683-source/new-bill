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
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val filtered = remember(products, searchQuery) {
        products.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, true) ||
                    it.hindiName.contains(searchQuery, true) ||
                    it.barcode.contains(searchQuery) ||
                    it.category.contains(searchQuery, true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("दुकान का सामान (Products)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KiranaNavyText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = KiranaGreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("नया सामान / Add Product", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_product_fab")
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
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("products_search_input"),
                placeholder = { Text("Search by name, barcode, category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KiranaNavySubtle) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "कुल सामान: ${filtered.size} प्रोडक्ट्स",
                style = MaterialTheme.typography.labelMedium.copy(color = KiranaNavySubtle)
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("कोई सामान नहीं मिला / No products found", color = KiranaNavySubtle)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { product ->
                        ProductManagementCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (isAddingNew || editingProduct != null) {
        ProductFormDialog(
            initialProduct = editingProduct,
            onDismiss = {
                isAddingNew = false
                editingProduct = null
            },
            onSave = { id, name, hindiName, barcode, cat, pPrice, sPrice, unit, stock, minStock, tax ->
                viewModel.saveProduct(id, name, hindiName, barcode, cat, pPrice, sPrice, unit, stock, minStock, tax)
                isAddingNew = false
                editingProduct = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("सामान हटाएं? / Delete Product") },
            text = { Text("क्या आप सच में '${productToDelete!!.name}' को हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
                ) {
                    Text("Delete / हटाएं")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProductManagementCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_row_${product.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KiranaNavyText
                        )
                    )
                    if (product.hindiName.isNotBlank()) {
                        Text(
                            text = product.hindiName,
                            style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                        )
                    }
                    Text(
                        text = "${product.category} • Barcode: ${if (product.barcode.isNotBlank()) product.barcode else "N/A"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = UpiBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UdhaarRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("खरीद मूल्य (Cost)", fontSize = 10.sp, color = KiranaNavySubtle)
                        Text("₹${product.purchasePrice.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Column {
                        Text("बिक्री मूल्य (Sale)", fontSize = 10.sp, color = KiranaNavySubtle)
                        Text(
                            "₹${product.sellingPrice.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KiranaGreenDark
                        )
                    }
                    val profitMargin = product.sellingPrice - product.purchasePrice
                    Column {
                        Text("मुनाफा (Profit)", fontSize = 10.sp, color = KiranaNavySubtle)
                        Text("+₹${profitMargin.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KiranaGreenPrimary)
                    }
                }

                val isLow = product.stockQuantity <= product.minStockAlert
                Surface(
                    color = if (isLow) AmberWarningBg else KiranaGreenLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    val stockStr = if (product.stockQuantity % 1.0 == 0.0) product.stockQuantity.toInt().toString() else product.stockQuantity.toString()
                    Text(
                        text = "स्टॉक: $stockStr ${product.unit}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLow) Color(0xFF78350F) else KiranaGreenDark
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        hindiName: String,
        barcode: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        unit: String,
        stockQuantity: Double,
        minStockAlert: Double,
        taxRate: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var hindiName by remember { mutableStateOf(initialProduct?.hindiName ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Flours & Grains / अनाज") }
    var purchasePriceText by remember { mutableStateOf(initialProduct?.purchasePrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var sellingPriceText by remember { mutableStateOf(initialProduct?.sellingPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "packet") }
    var stockText by remember { mutableStateOf(initialProduct?.stockQuantity?.let { if (it > 0) it.toInt().toString() else "" } ?: "10") }
    var minStockText by remember { mutableStateOf(initialProduct?.minStockAlert?.let { if (it > 0) it.toInt().toString() else "" } ?: "5") }
    var taxRateText by remember { mutableStateOf(initialProduct?.taxRate?.let { if (it > 0) it.toInt().toString() else "" } ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProduct == null) "नया सामान जोड़ें / Add Product" else "सामान बदलें / Edit Product") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name (English) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = hindiName,
                        onValueChange = { hindiName = it },
                        label = { Text("सामान का नाम (हिंदी)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / बारकोड") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / श्रेणी") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePriceText,
                            onValueChange = { purchasePriceText = it },
                            label = { Text("खरीद मूल्य (Cost) ₹ *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = { sellingPriceText = it },
                            label = { Text("बिक्री मूल्य (Sale) ₹ *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Stock Qty *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (kg/packet/litre)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minStockText,
                            onValueChange = { minStockText = it },
                            label = { Text("Min Stock Alert") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = taxRateText,
                            onValueChange = { taxRateText = it },
                            label = { Text("GST % (0, 5, 12, 18)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = purchasePriceText.toDoubleOrNull() ?: 0.0
                    val sPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toDoubleOrNull() ?: 0.0
                    val minStock = minStockText.toDoubleOrNull() ?: 5.0
                    val tax = taxRateText.toDoubleOrNull() ?: 0.0

                    if (name.isNotBlank() && sPrice > 0) {
                        onSave(
                            initialProduct?.id ?: 0L,
                            name,
                            hindiName,
                            barcode,
                            category,
                            pPrice,
                            sPrice,
                            unit,
                            stock,
                            minStock,
                            tax
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Save / सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
