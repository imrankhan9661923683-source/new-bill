package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.domain.model.CartItem
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingPosScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showSplitPaymentDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Categories
    val categories = remember(products) {
        listOf("ALL") + products.map { it.category }.distinct()
    }

    // Filtered products
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            (selectedCategory == "ALL" || p.category == selectedCategory) &&
                    (searchQuery.isBlank() ||
                            p.name.contains(searchQuery, ignoreCase = true) ||
                            p.hindiName.contains(searchQuery, ignoreCase = true) ||
                            p.barcode.contains(searchQuery))
        }
    }

    // Cart calculations
    val subtotal = remember(cartItems) { cartItems.sumOf { it.lineTotal } }
    val taxAmount = remember(cartItems) { cartItems.sumOf { it.taxAmount } }
    val finalAmount = remember(subtotal, discountAmount, taxAmount) {
        (subtotal - discountAmount + taxAmount).coerceAtLeast(0.0)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(PosBg)
            .testTag("pos_billing_screen")
    ) {
        val isWide = maxWidth > 720.dp

        if (isWide) {
            // Tablet / Desktop Dual-Pane POS
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Catalog Pane
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(14.dp)
                ) {
                    PosHeaderAndSearch(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onBack = onNavigateBack,
                        onAddCustomItem = { showCustomItemDialog = true }
                    )

                    CategoryFilterRow(
                        categories = categories,
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ProductCatalogList(
                        products = filteredProducts,
                        onAddToCart = { viewModel.addToCart(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                VerticalDivider(color = DividerColor)

                // Right Cart & Checkout Pane
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .background(SurfaceWhite)
                        .padding(14.dp)
                ) {
                    CartCustomerHeader(
                        customer = selectedCustomer,
                        onSelectCustomerClick = { showCustomerPicker = true },
                        onClearCustomer = { viewModel.selectCustomer(null) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CartItemList(
                        items = cartItems,
                        onUpdateQty = { id, q -> viewModel.updateCartItemQuantity(id, q) },
                        onRemove = { viewModel.removeFromCart(it) },
                        onClearAll = { viewModel.clearCart() },
                        modifier = Modifier.weight(1f)
                    )

                    CheckoutSummaryBottom(
                        subtotal = subtotal,
                        discount = discountAmount,
                        tax = taxAmount,
                        finalAmount = finalAmount,
                        paymentMethod = paymentMethod,
                        onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                        onDiscountClick = { showDiscountDialog = true },
                        onSplitClick = { showSplitPaymentDialog = true },
                        onGenerateBill = {
                            viewModel.completeBill(
                                onSuccess = { invoice ->
                                    viewModel.viewInvoice(invoice.id)
                                },
                                onError = { msg -> errorMessage = msg }
                            )
                        }
                    )
                }
            }
        } else {
            // Mobile Layout with Tab/BottomSheet Cart
            var activeTab by remember { mutableStateOf(0) } // 0 = Catalog, 1 = Cart

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Tab Bar (Catalog vs Cart)
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
                                "सामान चुनें / Catalog",
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = null) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            BadgeBox(count = cartItems.size) {
                                Text(
                                    "बिल कार्ट (₹${finalAmount.toInt()})",
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                    )
                }

                if (activeTab == 0) {
                    // Catalog view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        PosHeaderAndSearch(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onBack = onNavigateBack,
                            onAddCustomItem = { showCustomItemDialog = true }
                        )

                        CategoryFilterRow(
                            categories = categories,
                            selected = selectedCategory,
                            onSelect = { selectedCategory = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProductCatalogList(
                            products = filteredProducts,
                            onAddToCart = { viewModel.addToCart(it) },
                            modifier = Modifier.weight(1f)
                        )

                        // Bottom Floating Mini Cart bar if items > 0
                        if (cartItems.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeTab = 1 }
                                    .padding(top = 8.dp)
                                    .testTag("open_cart_bar"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = KiranaNavyHeader)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "${cartItems.size} सामान कार्ट में / Items",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                        )
                                        Text(
                                            FormatUtils.formatRupee(finalAmount),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    Button(
                                        onClick = { activeTab = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("बिल देखें / View Cart")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Cart & Checkout view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceWhite)
                            .padding(14.dp)
                    ) {
                        CartCustomerHeader(
                            customer = selectedCustomer,
                            onSelectCustomerClick = { showCustomerPicker = true },
                            onClearCustomer = { viewModel.selectCustomer(null) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CartItemList(
                            items = cartItems,
                            onUpdateQty = { id, q -> viewModel.updateCartItemQuantity(id, q) },
                            onRemove = { viewModel.removeFromCart(it) },
                            onClearAll = { viewModel.clearCart() },
                            modifier = Modifier.weight(1f)
                        )

                        CheckoutSummaryBottom(
                            subtotal = subtotal,
                            discount = discountAmount,
                            tax = taxAmount,
                            finalAmount = finalAmount,
                            paymentMethod = paymentMethod,
                            onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                            onDiscountClick = { showDiscountDialog = true },
                            onSplitClick = { showSplitPaymentDialog = true },
                            onGenerateBill = {
                                viewModel.completeBill(
                                    onSuccess = { invoice ->
                                        viewModel.viewInvoice(invoice.id)
                                    },
                                    onError = { msg -> errorMessage = msg }
                                )
                            }
                        )
                    }
                }
            }
        }

        // Error Snackbar / Dialog
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Billing Alert / सूचना") },
                text = { Text(errorMessage!!) },
                confirmButton = {
                    Button(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }

        // Customer Picker Dialog
        if (showCustomerPicker) {
            CustomerPickerDialog(
                customers = customers,
                onSelect = {
                    viewModel.selectCustomer(it)
                    showCustomerPicker = false
                },
                onDismiss = { showCustomerPicker = false },
                onAddNewCustomer = { name, phone ->
                    viewModel.saveCustomer(name = name, phone = phone, address = "", creditLimit = 5000.0)
                }
            )
        }

        // Discount Dialog
        if (showDiscountDialog) {
            DiscountInputDialog(
                currentDiscount = discountAmount,
                onApply = {
                    viewModel.setDiscount(it)
                    showDiscountDialog = false
                },
                onDismiss = { showDiscountDialog = false }
            )
        }

        // Split Payment Dialog
        if (showSplitPaymentDialog) {
            SplitPaymentDialog(
                totalAmount = finalAmount,
                onConfirm = { cash, upi, udhaar ->
                    viewModel.setSplitAmounts(cash, upi, udhaar)
                    viewModel.setPaymentMethod("SPLIT")
                    showSplitPaymentDialog = false
                },
                onDismiss = { showSplitPaymentDialog = false }
            )
        }

        // Custom Item Add Dialog
        if (showCustomItemDialog) {
            CustomItemAddDialog(
                onAdd = { name, price, qty, unit ->
                    val customProduct = ProductEntity(
                        id = 0,
                        name = name,
                        purchasePrice = price * 0.8,
                        sellingPrice = price,
                        unit = unit,
                        stockQuantity = 999.0
                    )
                    viewModel.addToCart(customProduct, qty)
                    showCustomItemDialog = false
                },
                onDismiss = { showCustomItemDialog = false }
            )
        }
    }
}

@Composable
private fun BadgeBox(count: Int, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        content()
        if (count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KiranaGreenPrimary)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PosHeaderAndSearch(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onAddCustomItem: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KiranaNavyText)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .testTag("pos_search_input"),
            placeholder = { Text("Search सामान / Barcode...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KiranaNavySubtle) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
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

        Button(
            onClick = onAddCustomItem,
            colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("अन्य / Misc", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat == selected
            val label = if (cat == "ALL") "All / सभी" else cat.substringBefore("/")
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(cat) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KiranaGreenPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ProductCatalogList(
    products: List<ProductEntity>,
    onAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("कोई सामान नहीं मिला / No items found", color = KiranaNavySubtle)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddToCart(product) }
                        .testTag("catalog_item_${product.id}"),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                val isLowStock = product.stockQuantity <= product.minStockAlert
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isLowStock) AmberWarningBg else KiranaGreenLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    val stockStr = if (product.stockQuantity % 1.0 == 0.0) product.stockQuantity.toInt().toString() else product.stockQuantity.toString()
                                    Text(
                                        text = "स्टॉक: $stockStr ${product.unit}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) Color(0xFF78350F) else KiranaGreenDark
                                    )
                                }

                                if (product.taxRate > 0) {
                                    Text(
                                        text = "GST: ${product.taxRate.toInt()}%",
                                        fontSize = 10.sp,
                                        color = KiranaNavySubtle
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "₹${product.sellingPrice.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KiranaGreenDark
                                )
                            )

                            Button(
                                onClick = { onAddToCart(product) },
                                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartCustomerHeader(
    customer: CustomerEntity?,
    onSelectCustomerClick: () -> Unit,
    onClearCustomer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (customer != null) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectCustomerClick() }
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (customer != null) UpiBlue else KiranaNavySubtle,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = customer?.name ?: "नकद ग्राहक / Walk-in Customer",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KiranaNavyText
                        )
                    )
                    if (customer != null && customer.totalUdhaar > 0) {
                        Text(
                            text = "बकाया उधार: ${FormatUtils.formatRupee(customer.totalUdhaar)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = UdhaarRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    } else {
                        Text(
                            text = "ग्राहक बदलें / Tap to select customer",
                            style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
                        )
                    }
                }
            }

            if (customer != null) {
                IconButton(onClick = onClearCustomer, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear customer", tint = Color.Gray)
                }
            } else {
                OutlinedButton(
                    onClick = onSelectCustomerClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Select", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CartItemList(
    items: List<CartItem>,
    onUpdateQty: (Long, Double) -> Unit,
    onRemove: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "बिल में सामान (${items.size} Items)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = KiranaNavyText
                )
            )

            if (items.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Clear All / हटाएं", color = UdhaarRed, fontSize = 12.sp)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "बिल में सामान जोड़ें",
                        style = MaterialTheme.typography.bodyMedium.copy(color = KiranaNavySubtle)
                    )
                    Text(
                        "Tap + on any product from the catalog",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = PosBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KiranaNavyText
                                    )
                                )
                                Text(
                                    text = "₹${item.unitPrice.toInt()} / ${item.product.unit}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
                                )
                            }

                            // Quantity Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onUpdateQty(item.product.id, item.quantity - 1.0) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        if (item.quantity <= 1.0) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = if (item.quantity <= 1.0) UdhaarRed else KiranaNavyText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                val qtyFormatted = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                                Text(
                                    text = qtyFormatted,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = KiranaNavyText),
                                    modifier = Modifier.widthIn(min = 20.dp),
                                    textAlign = TextAlign.Center
                                )

                                IconButton(
                                    onClick = { onUpdateQty(item.product.id, item.quantity + 1.0) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = KiranaGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "₹${item.lineTotal.toInt()}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KiranaGreenDark
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutSummaryBottom(
    subtotal: Double,
    discount: Double,
    tax: Double,
    finalAmount: Double,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onDiscountClick: () -> Unit,
    onSplitClick: () -> Unit,
    onGenerateBill: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        HorizontalDivider(color = DividerColor)
        Spacer(modifier = Modifier.height(6.dp))

        // Subtotal, Discount & Tax row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Subtotal: ${FormatUtils.formatRupee(subtotal)}",
                style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (discount > 0) "Discount: -₹${discount.toInt()}" else "+ Discount (छूट)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (discount > 0) UdhaarRed else UpiBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onDiscountClick() }
                )

                if (tax > 0) {
                    Text(
                        text = "GST: +₹${tax.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(color = KiranaNavySubtle)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Payment Method Chips (Cash, UPI, Udhaar, Split)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PaymentModeChip(
                label = "नकद (Cash)",
                isSelected = paymentMethod == "CASH",
                onClick = { onPaymentMethodChange("CASH") },
                color = KiranaGreenPrimary,
                modifier = Modifier.weight(1f)
            )
            PaymentModeChip(
                label = "UPI / QR",
                isSelected = paymentMethod == "UPI",
                onClick = { onPaymentMethodChange("UPI") },
                color = UpiBlue,
                modifier = Modifier.weight(1f)
            )
            PaymentModeChip(
                label = "उधार (Udhaar)",
                isSelected = paymentMethod == "UDHAAR",
                onClick = { onPaymentMethodChange("UDHAAR") },
                color = UdhaarRed,
                modifier = Modifier.weight(1f)
            )
            PaymentModeChip(
                label = "Split",
                isSelected = paymentMethod == "SPLIT",
                onClick = { onSplitClick() },
                color = AmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Green Bill Button
        Button(
            onClick = onGenerateBill,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_bill_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = KiranaGreenPrimary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "बिल बनाएं / PRINT BILL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = FormatUtils.formatRupee(finalAmount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
private fun PaymentModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color else PosBg,
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else KiranaNavyText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CustomerPickerDialog(
    customers: List<CustomerEntity>,
    onSelect: (CustomerEntity) -> Unit,
    onDismiss: () -> Unit,
    onAddNewCustomer: (String, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    val filtered = remember(customers, query) {
        customers.filter {
            query.isBlank() || it.name.contains(query, true) || it.phone.contains(query)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ग्राहक चुनें / Select Customer") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!showAddForm) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name / phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { customer ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(customer) },
                                colors = CardDefaults.cardColors(containerColor = PosBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold)
                                        if (customer.phone.isNotEmpty()) {
                                            Text(customer.phone, fontSize = 12.sp, color = KiranaNavySubtle)
                                        }
                                    }
                                    if (customer.totalUdhaar > 0) {
                                        Text(
                                            "उधार: ₹${customer.totalUdhaar.toInt()}",
                                            color = UdhaarRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showAddForm = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ नया ग्राहक जोड़ें / New Customer")
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Customer Name / ग्राहक का नाम") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Mobile Number / मोबाइल नंबर") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (showAddForm) {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddNewCustomer(newName, newPhone)
                            showAddForm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
                ) {
                    Text("Save Customer")
                }
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
private fun DiscountInputDialog(
    currentDiscount: Double,
    onApply: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var discountText by remember { mutableStateOf(if (currentDiscount > 0) currentDiscount.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("छूट दें / Apply Discount (₹)") },
        text = {
            OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("Discount Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val disc = discountText.toDoubleOrNull() ?: 0.0
                    onApply(disc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Apply")
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
private fun SplitPaymentDialog(
    totalAmount: Double,
    onConfirm: (cash: Double, upi: Double, udhaar: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var cashText by remember { mutableStateOf("") }
    var upiText by remember { mutableStateOf("") }
    var udhaarText by remember { mutableStateOf("") }

    val enteredTotal = (cashText.toDoubleOrNull() ?: 0.0) +
            (upiText.toDoubleOrNull() ?: 0.0) +
            (udhaarText.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Split Payment / मिला-जुला भुगतान") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Total Bill: ${FormatUtils.formatRupee(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = KiranaGreenDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cashText,
                    onValueChange = { cashText = it },
                    label = { Text("नकद (Cash) ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = upiText,
                    onValueChange = { upiText = it },
                    label = { Text("ऑनलाइन (UPI) ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = udhaarText,
                    onValueChange = { udhaarText = it },
                    label = { Text("उधार (Udhaar) ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Entered Total: ₹${enteredTotal.toInt()} / ₹${totalAmount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = if (Math.abs(enteredTotal - totalAmount) < 1.0) KiranaGreenDark else UdhaarRed
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        cashText.toDoubleOrNull() ?: 0.0,
                        upiText.toDoubleOrNull() ?: 0.0,
                        udhaarText.toDoubleOrNull() ?: 0.0
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Confirm Split")
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
private fun CustomItemAddDialog(
    onAdd: (name: String, price: Double, qty: Double, unit: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("piece") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("अन्य सामान जोड़ें / Add Misc Item") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name / सामान का नाम") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (kg/pk)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val qty = qtyText.toDoubleOrNull() ?: 1.0
                    if (name.isNotBlank() && price > 0) {
                        onAdd(name, price, qty, unit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary)
            ) {
                Text("Add to Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
