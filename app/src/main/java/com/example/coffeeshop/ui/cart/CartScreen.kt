package com.example.coffeeshop.ui.cart

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    goBack: () -> Unit,
    goHome: () -> Unit,
    vm: CartViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cartItems = vm.cartItems
    val productCache = vm.productCache

    // Math State
    val subtotal = vm.getSubtotal()
    val discount = vm.couponDiscountAmount.value
    val tax = vm.getTaxAmount()
    val grandTotal = vm.getGrandTotal()

    var selectedPayment by remember { mutableStateOf("ONLINE") }
    var couponInput by remember { mutableStateOf("") }

    // NEW: Coupon Bottom Sheet State
    var showCouponSheet by remember { mutableStateOf(false) }

    val isPlacingOrder by vm.isPlacingOrder
    val orderSuccess by vm.orderSuccess
    val errorMessage by vm.errorMessage
    val appliedCoupon by vm.appliedCoupon

    // Strict UPI Launcher
    val upiLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data?.getStringExtra("response")?.lowercase() ?: ""
        if (data.isNotEmpty() && (data.contains("status=success") || data.contains("responsecode=00"))) {
            vm.placeOrder("ONLINE")
        } else {
            vm.errorMessage.value = "Payment failed or was cancelled."
        }
    }

    if (orderSuccess) { OrderSuccessAnimation(goHome = goHome); return }

    // --- COUPON BOTTOM SHEET ---
    if (showCouponSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showCouponSheet = false },
            sheetState = sheetState,
            containerColor = Cream,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                Text("AVAILABLE COUPONS", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown)
                Spacer(Modifier.height(16.dp))

                val coupons = listOf(
                    Triple("WELCOME20", "20% Off First Order", "Welcome to the family! Valid only on your first order."),
                    Triple("SAVE10", "10% Off", "Valid on orders above ₹500."),
                    Triple("MEGA25", "25% Off", "Valid on premium orders above ₹2000."),
                    Triple("GPAY5", "5% Off Online", "Valid exclusively for Online/UPI Payments.")
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(coupons) { (code, title, desc) ->
                        CouponCard(code, title, desc) {
                            vm.applyCoupon(code, selectedPayment)
                            showCouponSheet = false
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YOUR CART", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                navigationIcon = { IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream,
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(color = Color.White, shadowElevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {

                        // Error Message
                        if (errorMessage != null) {
                            Text(errorMessage!!, fontFamily = Montserrat, fontSize = 12.sp, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        // Grand Total Display
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("GRAND TOTAL", fontFamily = Montserrat, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                            Text("₹$grandTotal", fontFamily = BebasNeue, fontSize = 32.sp, color = CoffeeBrown)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (selectedPayment == "ONLINE") {
                                    val uri = Uri.parse("upi://pay?pa=affanshk021@oksbi&pn=Brew-N-Beans&tn=Coffee Order&am=$grandTotal&cu=INR")
                                    val upiIntent = Intent(Intent.ACTION_VIEW, uri)
                                    val chooser = Intent.createChooser(upiIntent, "Pay with...")
                                    try { upiLauncher.launch(chooser) } catch (e: Exception) { vm.errorMessage.value = "No UPI app found." }
                                } else {
                                    vm.placeOrder("COD")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                            enabled = !isPlacingOrder
                        ) {
                            if (isPlacingOrder) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text(if (selectedPayment == "ONLINE") "PAY ONLINE • ₹$grandTotal" else "PLACE COD ORDER", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", fontFamily = Montserrat, fontSize = 16.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 180.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cart Items
                items(cartItems.keys.toList()) { productId ->
                    val pair = cartItems[productId]
                    val product = productCache[productId]
                    if (pair != null && product != null) {
                        CartItemCard(
                            product = product,
                            quantity = pair.first,
                            instructions = pair.second,
                            onAdd = { vm.addOne(product) },
                            onRemove = { vm.removeOne(product) },
                            onInstructionChange = { vm.updateInstructions(product.id, it) }
                        )
                    }
                }

                // Payment Method
                item {
                    Spacer(Modifier.height(10.dp))
                    Text("PAYMENT METHOD", fontFamily = BebasNeue, fontSize = 22.sp, color = CoffeeBrown)
                    Spacer(Modifier.height(8.dp))
                    PaymentOption(title = "Pay Online (UPI/Card)", icon = Icons.Default.Payment, isSelected = selectedPayment == "ONLINE", onClick = { selectedPayment = "ONLINE" })
                    Spacer(Modifier.height(8.dp))
                    PaymentOption(title = "Cash on Delivery", icon = Icons.Default.DeliveryDining, isSelected = selectedPayment == "COD", onClick = { selectedPayment = "COD"; if(appliedCoupon == "GPAY5") vm.removeCoupon() })
                }

                // Coupons Section
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("APPLY COUPON", fontFamily = BebasNeue, fontSize = 22.sp, color = CoffeeBrown)
                    Spacer(Modifier.height(8.dp))

                    if (appliedCoupon != null) {
                        // Coupon Applied Card
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFF4CAF50))
                                    Spacer(Modifier.width(8.dp))
                                    Text("'$appliedCoupon' Applied!", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                IconButton(onClick = { vm.removeCoupon() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                                }
                            }
                        }
                    } else {
                        // Coupon Input Field
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it.uppercase() },
                                placeholder = { Text("Enter code", fontFamily = Montserrat, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { vm.applyCoupon(couponInput, selectedPayment) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                                modifier = Modifier.height(56.dp)
                            ) { Text("APPLY", fontFamily = BebasNeue, fontSize = 16.sp) }
                        }
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "View all available coupons",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CoffeeBrown,
                            modifier = Modifier
                                .clickable { showCouponSheet = true }
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                // Bill Summary
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("BILL SUMMARY", fontFamily = BebasNeue, fontSize = 22.sp, color = CoffeeBrown)
                    Spacer(Modifier.height(12.dp))

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // ✅ FIXED: Using .entries to correctly access the product key and quantity
                            val pureTotal = vm.cartItems.entries.sumOf { entry ->
                                val product = vm.productCache[entry.key]
                                val quantity = entry.value.first
                                (product?.price ?: 0) * quantity
                            }

                            BillRow("Item Total", "₹$pureTotal")

                            // Show BOGO Savings if Subtotal is lower than pure Item Total
                            if (pureTotal > subtotal) {
                                Spacer(Modifier.height(8.dp))
                                BillRow("BOGO Discount", "-₹${pureTotal - subtotal}", color = Color(0xFF4CAF50))
                            }

                            if (appliedCoupon != null) {
                                Spacer(Modifier.height(8.dp))
                                BillRow("Coupon ($appliedCoupon)", "-₹$discount", color = Color(0xFF4CAF50))
                            }

                            Spacer(Modifier.height(8.dp))
                            BillRow("CGST (2.5%)", "₹${tax / 2}")
                            Spacer(Modifier.height(8.dp))
                            BillRow("SGST (2.5%)", "₹${tax / 2}")

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            Spacer(Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TO PAY", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Text("₹$grandTotal", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoffeeBrown)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Reusable Coupon Card for the Bottom Sheet
@Composable
fun CouponCard(code: String, title: String, desc: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8F5E9)).padding(12.dp)) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(code, fontFamily = BebasNeue, fontSize = 20.sp, color = CoffeeBrown)
                    Spacer(Modifier.width(8.dp))
                    Text("• $title", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                }
                Spacer(Modifier.height(4.dp))
                Text(desc, fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun BillRow(title: String, amount: String, color: Color = Color.DarkGray) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontFamily = Montserrat, fontSize = 14.sp, color = color)
        Text(amount, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun OrderSuccessAnimation(goHome: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (startAnimation) 1.2f else 0f, animationSpec = tween(durationMillis = 800))
    LaunchedEffect(Unit) { startAnimation = true; delay(2500); goHome() }
    Column(modifier = Modifier.fillMaxSize().background(Cream), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = CoffeeBrown, modifier = Modifier.size(100.dp).scale(scale))
        Spacer(Modifier.height(24.dp))
        Text("ORDER PLACED!", fontFamily = BebasNeue, fontSize = 40.sp, color = CoffeeBrown)
        Spacer(Modifier.height(12.dp))
        Text("Your payment is confirmed.\nRedirecting to Home...", fontFamily = Montserrat, fontSize = 16.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun CartItemCard(product: com.example.coffeeshop.data.model.Product, quantity: Int, instructions: String, onAdd: () -> Unit, onRemove: () -> Unit, onInstructionChange: (String) -> Unit) {
    val context = LocalContext.current
    val imageResId = remember(product.imageResName) { context.resources.getIdentifier(product.imageResName, "drawable", context.packageName) }
    val isBogoEligible = product.name == "Java Chip Frappe" || product.name == "Caramel Macchiato"

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Cream)) {
                    if (product.imageResName.startsWith("http")) {
                        AsyncImage(model = product.imageResName, contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (imageResId != 0) {
                        Image(painterResource(id = imageResId), contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontFamily = BebasNeue, fontSize = 20.sp, color = Color.Black)
                    if (isBogoEligible) {
                        Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("BUY 1 GET 1 FREE", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFE65100), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Text("₹${product.price}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoffeeBrown)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(CoffeeBrown)) {
                    Text("-", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { onRemove() }.padding(horizontal = 12.dp, vertical = 6.dp))
                    Text(quantity.toString(), color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("+", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { onAdd() }.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = instructions, onValueChange = onInstructionChange, placeholder = { Text("Any cooking instructions?", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown, unfocusedBorderColor = Color(0xFFE0E0E0)), textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Montserrat, fontSize = 12.sp))
        }
    }
}

@Composable
fun PaymentOption(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Cream else Color.White
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bgColor).clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = CoffeeBrown)
    }
}