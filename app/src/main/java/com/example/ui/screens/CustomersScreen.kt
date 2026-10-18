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
import com.example.data.entity.CustomerEntity
import com.example.ui.KiranaViewModel
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: KiranaViewModel,
    onNavigateBack: () -> Unit,
    onOpenKhata: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isAddingCustomer by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val filtered = remember(customers, searchQuery) {
        customers.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.phone.contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ग्राहक सूची (Customers)", fontWeight = FontWeight.Bold, color = KiranaNavyText) },
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
                onClick = { isAddingCustomer = true },
                containerColor = KiranaGreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("नया ग्राहक / Add Customer", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_customer_fab")
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
            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                placeholder = { Text("Search by name or phone...") },
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
                "कुल ग्राहक: ${filtered.size}",
                style = MaterialTheme.typography.labelMedium.copy(color = KiranaNavySubtle)
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("कोई ग्राहक नहीं मिला / No customers found", color = KiranaNavySubtle)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_item_${customer.id}"),
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
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(KiranaGreenLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = KiranaGreenDark,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, color = KiranaNavyText)
                                        if (customer.phone.isNotEmpty()) {
                                            Text(customer.phone, fontSize = 12.sp, color = KiranaNavySubtle)
                                        }
                                        if (customer.address.isNotEmpty()) {
                                            Text(customer.address, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (customer.totalUdhaar > 0) {
                                        Text(
                                            text = FormatUtils.formatRupee(customer.totalUdhaar),
                                            fontWeight = FontWeight.Bold,
                                            color = UdhaarRed,
                                            fontSize = 14.sp
                                        )
                                        Text("बाकी उधार", fontSize = 10.sp, color = UdhaarRed)
                                    } else {
                                        Text("₹0", fontWeight = FontWeight.Bold, color = JamaGreen, fontSize = 14.sp)
                                        Text("साफ खाता", fontSize = 10.sp, color = JamaGreen)
                                    }

                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        IconButton(onClick = { editingCustomer = customer }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = UpiBlue, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { customerToDelete = customer }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UdhaarRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    if (isAddingCustomer || editingCustomer != null) {
        CustomerFormDialog(
            initial = editingCustomer,
            onDismiss = {
                isAddingCustomer = false
                editingCustomer = null
            },
            onSave = { id, name, phone, address, creditLimit ->
                viewModel.saveCustomer(id, name, phone, address, creditLimit)
                isAddingCustomer = false
                editingCustomer = null
            }
        )
    }

    // Delete Confirmation
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("ग्राहक हटाएं? / Delete Customer") },
            text = { Text("क्या आप सच में '${customerToDelete!!.name}' को हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customerToDelete!!)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CustomerFormDialog(
    initial: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, phone: String, address: String, creditLimit: Double) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var creditLimitText by remember { mutableStateOf(initial?.creditLimit?.toInt()?.toString() ?: "5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "नया ग्राहक जोड़ें / New Customer" else "ग्राहक विवरण बदलें / Edit") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name / ग्राहक का नाम *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone / मोबाइल") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / पता") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = creditLimitText,
                    onValueChange = { creditLimitText = it },
                    label = { Text("Credit Limit (उधार सीमा) ₹") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val limit = creditLimitText.toDoubleOrNull() ?: 5000.0
                        onSave(initial?.id ?: 0L, name, phone, address, limit)
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
