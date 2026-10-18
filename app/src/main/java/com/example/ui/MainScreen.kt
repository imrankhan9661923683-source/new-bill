package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ShopInfo
import com.example.ui.components.InvoiceReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ScreenDestination(val titleHinglish: String, val icon: ImageVector) {
    DASHBOARD("डैशबोर्ड", Icons.Default.Dashboard),
    NEW_BILL("नया बिल", Icons.Default.PointOfSale),
    PRODUCTS("सामान", Icons.Default.Inventory2),
    STOCK("स्टॉक", Icons.Default.Warehouse),
    CUSTOMERS("ग्राहक", Icons.Default.People),
    UDHAAR("उधार खाता", Icons.Default.MenuBook),
    PURCHASES("खरीद", Icons.Default.LocalShipping),
    DAILY_HISAAB("दैनिक हिसाब", Icons.Default.Calculate),
    REPORTS("रिपोर्ट्स", Icons.Default.Assessment),
    SETTINGS("सेटिंग्स", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: KiranaViewModel) {
    var currentScreen by remember { mutableStateOf(ScreenDestination.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val shopInfo by viewModel.shopInfo.collectAsState()
    val activeInvoiceDetail by viewModel.activeInvoiceDetail.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    // Handle back button: if not on DASHBOARD, return to DASHBOARD
    BackHandler(enabled = currentScreen != ScreenDestination.DASHBOARD) {
        currentScreen = ScreenDestination.DASHBOARD
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 760.dp

        if (isWideScreen) {
            // Adaptive NavigationRail for Tablets & Large Screens
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = SurfaceWhite,
                    contentColor = KiranaNavyText,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(KiranaGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Smart Kirana",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = KiranaNavyText
                            )
                        }
                    }
                ) {
                    ScreenDestination.values().forEach { dest ->
                        NavigationRailItem(
                            selected = currentScreen == dest,
                            onClick = { currentScreen = dest },
                            icon = {
                                if (dest == ScreenDestination.NEW_BILL && cartItems.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text("${cartItems.size}") } }) {
                                        Icon(dest.icon, contentDescription = dest.titleHinglish)
                                    }
                                } else {
                                    Icon(dest.icon, contentDescription = dest.titleHinglish)
                                }
                            },
                            label = { Text(dest.titleHinglish, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = KiranaGreenPrimary,
                                selectedTextColor = KiranaGreenDark,
                                indicatorColor = KiranaGreenLight
                            )
                        )
                    }
                }

                VerticalDivider(color = DividerColor)

                // Main Content Pane
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        shopInfo = shopInfo,
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        } else {
            // Mobile Smartphone Navigation with Drawer & BottomBar
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.widthIn(max = 300.dp),
                        drawerContainerColor = SurfaceWhite
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KiranaGreenLight)
                                .padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(KiranaGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = shopInfo.shopName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KiranaGreenDark
                                )
                            )
                            Text(
                                text = "स्मार्ट किराना बिलिंग व स्टॉक",
                                style = MaterialTheme.typography.bodySmall.copy(color = KiranaNavySubtle)
                            )
                        }

                        HorizontalDivider(color = DividerColor)

                        Spacer(modifier = Modifier.height(8.dp))

                        // All 9+ Destinations in Drawer
                        ScreenDestination.values().forEach { dest ->
                            NavigationDrawerItem(
                                label = { Text(dest.titleHinglish, fontWeight = FontWeight.SemiBold) },
                                icon = { Icon(dest.icon, contentDescription = null) },
                                selected = currentScreen == dest,
                                onClick = {
                                    currentScreen = dest
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = KiranaGreenLight,
                                    selectedIconColor = KiranaGreenDark,
                                    selectedTextColor = KiranaGreenDark
                                )
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentScreen.titleHinglish,
                                    fontWeight = FontWeight.Bold,
                                    color = KiranaNavyText
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = KiranaNavyText)
                                }
                            },
                            actions = {
                                if (currentScreen != ScreenDestination.NEW_BILL) {
                                    Button(
                                        onClick = { currentScreen = ScreenDestination.NEW_BILL },
                                        colors = ButtonDefaults.buttonColors(containerColor = KiranaGreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).padding(end = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("बिल / POS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceWhite,
                            contentColor = KiranaNavyText
                        ) {
                            val mainTabs = listOf(
                                ScreenDestination.DASHBOARD,
                                ScreenDestination.NEW_BILL,
                                ScreenDestination.STOCK,
                                ScreenDestination.UDHAAR,
                                ScreenDestination.DAILY_HISAAB
                            )

                            mainTabs.forEach { dest ->
                                val isSelected = currentScreen == dest
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = dest },
                                    icon = {
                                        if (dest == ScreenDestination.NEW_BILL && cartItems.isNotEmpty()) {
                                            BadgedBox(badge = { Badge { Text("${cartItems.size}") } }) {
                                                Icon(dest.icon, contentDescription = dest.titleHinglish)
                                            }
                                        } else {
                                            Icon(dest.icon, contentDescription = dest.titleHinglish)
                                        }
                                    },
                                    label = { Text(dest.titleHinglish, fontSize = 10.sp, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = KiranaGreenPrimary,
                                        selectedTextColor = KiranaGreenDark,
                                        indicatorColor = KiranaGreenLight
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ScreenContent(
                            currentScreen = currentScreen,
                            viewModel = viewModel,
                            shopInfo = shopInfo,
                            onNavigate = { currentScreen = it }
                        )
                    }
                }
            }
        }

        // Active Invoice Receipt Dialog Modal
        if (activeInvoiceDetail != null) {
            InvoiceReceiptDialog(
                detail = activeInvoiceDetail!!,
                shopInfo = shopInfo,
                onDismiss = { viewModel.dismissInvoiceDetail() }
            )
        }
    }
}

@Composable
private fun ScreenContent(
    currentScreen: ScreenDestination,
    viewModel: KiranaViewModel,
    shopInfo: ShopInfo,
    onNavigate: (ScreenDestination) -> Unit
) {
    when (currentScreen) {
        ScreenDestination.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            shopInfo = shopInfo,
            onNavigateToBilling = { onNavigate(ScreenDestination.NEW_BILL) },
            onNavigateToProducts = { onNavigate(ScreenDestination.PRODUCTS) },
            onNavigateToStock = { onNavigate(ScreenDestination.STOCK) },
            onNavigateToCustomers = { onNavigate(ScreenDestination.CUSTOMERS) },
            onNavigateToUdhaar = { onNavigate(ScreenDestination.UDHAAR) },
            onNavigateToPurchases = { onNavigate(ScreenDestination.PURCHASES) },
            onNavigateToHisaab = { onNavigate(ScreenDestination.DAILY_HISAAB) },
            onNavigateToReports = { onNavigate(ScreenDestination.REPORTS) }
        )
        ScreenDestination.NEW_BILL -> BillingPosScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.PRODUCTS -> ProductsScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.STOCK -> StockInventoryScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.CUSTOMERS -> CustomersScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) },
            onOpenKhata = { onNavigate(ScreenDestination.UDHAAR) }
        )
        ScreenDestination.UDHAAR -> UdhaarKhataScreen(
            viewModel = viewModel,
            shopInfo = shopInfo,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.PURCHASES -> PurchasesScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.DAILY_HISAAB -> DailyHisaabScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.REPORTS -> ReportsScreen(
            viewModel = viewModel,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
        ScreenDestination.SETTINGS -> SettingsScreen(
            viewModel = viewModel,
            shopInfo = shopInfo,
            onNavigateBack = { onNavigate(ScreenDestination.DASHBOARD) }
        )
    }
}
