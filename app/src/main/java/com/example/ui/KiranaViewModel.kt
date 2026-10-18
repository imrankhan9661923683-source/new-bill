package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.KiranaRepository
import com.example.domain.model.CartItem
import com.example.domain.model.DailySummary
import com.example.domain.model.InvoiceDetail
import com.example.domain.model.ShopInfo
import com.example.ui.util.FormatUtils
import com.example.ui.util.LanguageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KiranaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KiranaRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = KiranaRepository(database)
    }

    // Language State
    private val _languageMode = MutableStateFlow(LanguageMode.HINGLISH)
    val languageMode: StateFlow<LanguageMode> = _languageMode.asStateFlow()

    fun setLanguageMode(mode: LanguageMode) {
        _languageMode.value = mode
    }

    // Shop Info
    private val _shopInfo = MutableStateFlow(ShopInfo())
    val shopInfo: StateFlow<ShopInfo> = _shopInfo.asStateFlow()

    fun updateShopInfo(info: ShopInfo) {
        _shopInfo.value = info
    }

    // Data streams
    val products: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customersWithUdhaar: StateFlow<List<CustomerEntity>> = repository.getCustomersWithUdhaar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val udhaarTransactions: StateFlow<List<UdhaarTransactionEntity>> = repository.getAllUdhaarTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.getAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<PurchaseEntity>> = repository.getAllPurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements: StateFlow<List<StockMovementEntity>> = repository.getAllStockMovements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHisaab: StateFlow<DailyHisaabEntity?> = repository.getDailyHisaab(FormatUtils.getTodayDateString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // POS & Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _posCategoryFilter = MutableStateFlow("ALL")
    val posCategoryFilter: StateFlow<String> = _posCategoryFilter.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("CASH") // CASH, UPI, UDHAAR, SPLIT
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _splitCashPaid = MutableStateFlow(0.0)
    val splitCashPaid: StateFlow<Double> = _splitCashPaid.asStateFlow()

    private val _splitUpiPaid = MutableStateFlow(0.0)
    val splitUpiPaid: StateFlow<Double> = _splitUpiPaid.asStateFlow()

    private val _splitUdhaarPaid = MutableStateFlow(0.0)
    val splitUdhaarPaid: StateFlow<Double> = _splitUdhaarPaid.asStateFlow()

    // Invoice View Modal
    private val _activeInvoiceDetail = MutableStateFlow<InvoiceDetail?>(null)
    val activeInvoiceDetail: StateFlow<InvoiceDetail?> = _activeInvoiceDetail.asStateFlow()

    // Toast/Snackbar notification
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    // POS Operations
    fun setPosSearchQuery(query: String) {
        _posSearchQuery.value = query
    }

    fun setPosCategoryFilter(cat: String) {
        _posCategoryFilter.value = cat
    }

    fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity, unitPrice = product.sellingPrice))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = newQuantity)
            _cartItems.value = current
        }
    }

    fun updateCartItemPrice(productId: Long, newPrice: Double) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(unitPrice = newPrice.coerceAtLeast(0.0))
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _discountAmount.value = 0.0
        _paymentMethod.value = "CASH"
        _splitCashPaid.value = 0.0
        _splitUpiPaid.value = 0.0
        _splitUdhaarPaid.value = 0.0
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun setDiscount(discount: Double) {
        _discountAmount.value = discount.coerceAtLeast(0.0)
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setSplitAmounts(cash: Double, upi: Double, udhaar: Double) {
        _splitCashPaid.value = cash
        _splitUpiPaid.value = upi
        _splitUdhaarPaid.value = udhaar
    }

    fun completeBill(
        onSuccess: (InvoiceEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            onError("Cart is empty / कार्ट खाली है")
            return
        }

        val subtotal = items.sumOf { it.lineTotal }
        val tax = items.sumOf { it.taxAmount }
        val finalAmount = (subtotal - _discountAmount.value + tax).coerceAtLeast(0.0)

        val method = _paymentMethod.value
        val customer = _selectedCustomer.value

        var cashPaid = 0.0
        var upiPaid = 0.0
        var udhaarAmount = 0.0

        when (method) {
            "CASH" -> cashPaid = finalAmount
            "UPI" -> upiPaid = finalAmount
            "UDHAAR" -> {
                if (customer == null) {
                    onError("Please select a customer for Udhaar / उधार के लिए ग्राहक चुनें")
                    return
                }
                udhaarAmount = finalAmount
            }
            "SPLIT" -> {
                val totalSplit = _splitCashPaid.value + _splitUpiPaid.value + _splitUdhaarPaid.value
                if (Math.abs(totalSplit - finalAmount) > 1.0) {
                    onError("Split amounts must equal bill total (₹${finalAmount.toInt()})")
                    return
                }
                if (_splitUdhaarPaid.value > 0.0 && customer == null) {
                    onError("Select customer for split Udhaar / ग्राहक चुनें")
                    return
                }
                cashPaid = _splitCashPaid.value
                upiPaid = _splitUpiPaid.value
                udhaarAmount = _splitUdhaarPaid.value
            }
        }

        viewModelScope.launch {
            try {
                val invoice = withContext(Dispatchers.IO) {
                    repository.createBill(
                        cartItems = items,
                        customer = customer,
                        paymentMethod = method,
                        cashPaid = cashPaid,
                        upiPaid = upiPaid,
                        udhaarAmount = udhaarAmount,
                        discountAmount = _discountAmount.value
                    )
                }
                clearCart()
                onSuccess(invoice)
                showMessage("Bill generated: ${invoice.invoiceNumber}")
            } catch (e: Exception) {
                onError(e.message ?: "Failed to generate bill")
            }
        }
    }

    fun viewInvoice(invoiceId: Long) {
        viewModelScope.launch {
            val detail = withContext(Dispatchers.IO) {
                repository.getInvoiceDetail(invoiceId)
            }
            _activeInvoiceDetail.value = detail
        }
    }

    fun dismissInvoiceDetail() {
        _activeInvoiceDetail.value = null
    }

    // Product actions
    fun saveProduct(
        id: Long = 0,
        name: String,
        hindiName: String,
        barcode: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        unit: String,
        stockQty: Double,
        minStock: Double,
        taxRate: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val product = ProductEntity(
                id = id,
                name = name.trim(),
                hindiName = hindiName.trim(),
                barcode = barcode.trim(),
                category = category.trim(),
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                unit = unit,
                stockQuantity = stockQty,
                minStockAlert = minStock,
                taxRate = taxRate,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertProduct(product)
                showMessage("Product added / सामान जुड़ गया: $name")
            } else {
                repository.updateProduct(product)
                showMessage("Product updated / सामान अपडेट हुआ: $name")
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(product)
            showMessage("Deleted: ${product.name}")
        }
    }

    fun adjustStock(productId: Long, delta: Double, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adjustStock(productId, delta, reason)
            showMessage("Stock adjusted / स्टॉक अपडेट किया")
        }
    }

    // Customer & Udhaar actions
    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        creditLimit: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val customer = CustomerEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                creditLimit = creditLimit
            )
            if (id == 0L) {
                repository.insertCustomer(customer)
                showMessage("Customer added / ग्राहक जुड़ गया: $name")
            } else {
                repository.updateCustomer(customer)
                showMessage("Customer updated: $name")
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomer(customer)
            showMessage("Customer deleted: ${customer.name}")
        }
    }

    fun recordUdhaarRepayment(
        customer: CustomerEntity,
        amount: Double,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordUdhaarPayment(customer, amount, paymentMethod, notes)
            showMessage("Received ₹$amount from ${customer.name}")
        }
    }

    fun recordManualUdhaarGiven(
        customer: CustomerEntity,
        amount: Double,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordManualUdhaarGiven(customer, amount, notes)
            showMessage("Udhaar of ₹$amount given to ${customer.name}")
        }
    }

    // Purchases & Suppliers
    fun addSupplier(name: String, phone: String, company: String, address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSupplier(
                SupplierEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    company = company.trim(),
                    address = address.trim()
                )
            )
            showMessage("Supplier added: $name")
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSupplier(supplier)
            showMessage("Supplier removed")
        }
    }

    fun recordPurchase(
        supplier: SupplierEntity?,
        items: List<KiranaRepository.PurchaseItemInput>,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val purchase = repository.recordPurchase(supplier, items, notes)
                showMessage("Purchase recorded: ${purchase.purchaseNumber}")
            } catch (e: Exception) {
                showMessage("Failed: ${e.message}")
            }
        }
    }

    // Expenses
    fun recordExpense(category: String, amount: Double, paymentMode: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordExpense(category, amount, paymentMode, note)
            showMessage("Expense saved: ₹$amount ($category)")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
            showMessage("Expense deleted")
        }
    }

    // Daily Hisaab
    fun setOpeningCash(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setOpeningCash(FormatUtils.getTodayDateString(), amount)
            showMessage("Opening cash set: ₹$amount")
        }
    }

    fun closeDailyHisaab(closingCash: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.closeDailyHisaab(FormatUtils.getTodayDateString(), closingCash, notes)
            showMessage("Daily Hisaab closed successfully")
        }
    }

    // Data reset
    fun resetToSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetWithSampleData()
            clearCart()
            showMessage("Sample kirana data restored!")
        }
    }
}
