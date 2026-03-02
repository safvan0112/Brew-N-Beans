package com.example.coffeeshop.ui.cart

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
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    goBack: () -> Unit,
    vm: CartViewModel = hiltViewModel()
) {
    val cartItems = vm.cartItems
    val productCache = vm.productCache
    val totalPrice = vm.getTotalPrice()

    var selectedPayment by remember { mutableStateOf("ONLINE") }
    val isPlacingOrder by vm.isPlacingOrder
    val orderSuccess by vm.orderSuccess

    // If order is successful, show a beautiful success screen
    if (orderSuccess) {
        OrderSuccessView(goBack = goBack)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YOUR CART", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                navigationIcon = {
                    IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream,
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(20.dp)) {
                    Button(
                        onClick = { vm.placeOrder(selectedPayment) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                        enabled = !isPlacingOrder
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("PLACE ORDER • ₹$totalPrice", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Render Cart Items
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

                item {
                    Spacer(Modifier.height(10.dp))
                    Text("PAYMENT METHOD", fontFamily = BebasNeue, fontSize = 22.sp, color = CoffeeBrown)
                    Spacer(Modifier.height(12.dp))

                    // Payment Selectors
                    PaymentOption(
                        title = "Pay Online (UPI/Card)",
                        icon = Icons.Default.Payment,
                        isSelected = selectedPayment == "ONLINE",
                        onClick = { selectedPayment = "ONLINE" }
                    )
                    Spacer(Modifier.height(8.dp))
                    PaymentOption(
                        title = "Cash on Delivery",
                        icon = Icons.Default.DeliveryDining,
                        isSelected = selectedPayment == "COD",
                        onClick = { selectedPayment = "COD" }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    product: com.example.coffeeshop.data.model.Product,
    quantity: Int,
    instructions: String,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onInstructionChange: (String) -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(product.imageResName) {
        context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Cream)) {
                    if (imageResId != 0) {
                        Image(painterResource(id = imageResId), contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontFamily = BebasNeue, fontSize = 20.sp, color = Color.Black)
                    Text("₹${product.price}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoffeeBrown)
                }
                // Plus / Minus Controls
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(CoffeeBrown)) {
                    Text("-", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { onRemove() }.padding(horizontal = 12.dp, vertical = 6.dp))
                    Text(quantity.toString(), color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("+", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { onAdd() }.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Cooking Instructions Input
            OutlinedTextField(
                value = instructions,
                onValueChange = onInstructionChange,
                placeholder = { Text("Any cooking instructions? (e.g. less sugar)", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoffeeBrown,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Montserrat, fontSize = 12.sp)
            )
        }
    }
}

@Composable
fun PaymentOption(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) CoffeeBrown else Color(0xFFE0E0E0)
    val bgColor = if (isSelected) Cream else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = CoffeeBrown)
        }
    }
}

@Composable
fun OrderSuccessView(goBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Cream).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = CoffeeBrown, modifier = Modifier.size(100.dp))
        Spacer(Modifier.height(24.dp))
        Text("ORDER PLACED!", fontFamily = BebasNeue, fontSize = 40.sp, color = CoffeeBrown)
        Spacer(Modifier.height(12.dp))
        Text("Your coffee is brewing. We will update you shortly.", fontFamily = Montserrat, fontSize = 16.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = goBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
        ) {
            Text("BACK TO MENU", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
        }
    }
}