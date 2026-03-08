package com.example.coffeeshop.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.coffeeshop.data.model.Order
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.ui.menu.MenuViewModel
import com.example.coffeeshop.ui.tracker.OrderTrackerViewModel
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    logout: () -> Unit,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    vm: MenuViewModel = hiltViewModel(),
    trackerVm: OrderTrackerViewModel = hiltViewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val menuItems by vm.menuItems
    val activeOrder by trackerVm.activeOrder

    var showHero by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "GOOD MORNING,"
            in 12..16 -> "GOOD AFTERNOON,"
            in 17..20 -> "GOOD EVENING,"
            else -> "GOOD NIGHT,"
        }
    }

    LaunchedEffect(Unit) {
        showHero = true
        delay(150)
        showContent = true
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            AnimatedVisibility(
                visible = activeOrder != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                activeOrder?.let { order ->
                    ActiveOrderTrackerBar(
                        order = order,
                        onComplete = { trackerVm.completeOrder(order.id) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ================= HERO SECTION =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(brush = Brush.verticalGradient(colors = listOf(CoffeeBrown, Color(0xFF3E2723))))
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(greeting, fontFamily = Montserrat, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(user?.displayName ?: "COFFEE LOVER", fontFamily = BebasNeue, fontSize = 28.sp, color = Color.White)
                        }
                        val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "U"
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = initial, fontFamily = BebasNeue, fontSize = 24.sp, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    AnimatedVisibility(visible = showHero, enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(600)) + fadeIn(tween(600))) {
                        Column {
                            Text("IT'S A GREAT DAY\nFOR COFFEE.", fontFamily = BebasNeue, fontSize = 36.sp, color = Color.White, lineHeight = 38.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onMenuClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Cream)) {
                                Text("ORDER NOW", fontFamily = BebasNeue, fontSize = 18.sp, color = CoffeeBrown)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ================= STAGGERED CONTENT =================
            AnimatedVisibility(visible = showContent, enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(700)) + fadeIn(tween(700))) {
                Column {
                    Text("CATEGORIES", fontFamily = BebasNeue, fontSize = 24.sp, color = CoffeeBrown, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(12.dp))
                    val categories = listOf("Coffee", "Sandwiches", "Croissants", "Desserts")
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categories) { category ->
                            Surface(modifier = Modifier.clickable { onMenuClick() }, color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp) {
                                Text(category, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoffeeBrown, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE5D3B3)), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("SPECIAL OFFER", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CoffeeBrown)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("BUY 1 GET 1 FREE", fontFamily = BebasNeue, fontSize = 26.sp, color = Color.Black)
                                Text("On Java Chip & Caramel Macchiato!", fontFamily = Montserrat, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    val bestSellers = menuItems.shuffled().take(4)
                    if (bestSellers.isNotEmpty()) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Text("POPULAR NOW", fontFamily = BebasNeue, fontSize = 24.sp, color = CoffeeBrown)
                                Text("See All", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onMenuClick() })
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                bestSellers.take(2).forEach { item ->
                                    PremiumItemCard(item = item, modifier = Modifier.weight(1f).clickable { onProductClick(item.id) })
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                bestSellers.drop(2).take(2).forEach { item ->
                                    PremiumItemCard(item = item, modifier = Modifier.weight(1f).clickable { onProductClick(item.id) })
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(20.dp).clickable { onAboutClick() }, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("THE BREW-N-BEANS STORY", fontFamily = BebasNeue, fontSize = 22.sp, color = CoffeeBrown)
                                Spacer(Modifier.height(4.dp))
                                Text("Discover the passion behind our hand-crafted blends and artisanal bakes.", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("READ MORE", fontFamily = Montserrat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                            }
                            Spacer(Modifier.width(16.dp))
                            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(16.dp)).background(Cream)) {
                                Icon(painterResource(id = android.R.drawable.ic_menu_info_details), contentDescription = null, tint = CoffeeBrown, modifier = Modifier.align(Alignment.Center).size(30.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun ActiveOrderTrackerBar(order: Order, onComplete: () -> Unit) {
    val isReady = order.status == "Ready"
    val bgColor = if (isReady) Color(0xFF4CAF50) else Color(0xFFFFA726)
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bgColor).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (isReady) "ORDER IS READY!" else "PREPARING ORDER...", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
                Text(text = if (isReady) "Pick it up at the counter." else "We are brewing your coffee.", fontFamily = Montserrat, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
            }
            if (isReady) {
                Button(onClick = onComplete, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Text("GOT IT", fontFamily = BebasNeue, fontSize = 16.sp, color = bgColor)
                }
            } else {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
fun PremiumItemCard(item: Product, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageResId = remember(item.imageResName) {
        context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // ✅ NEW: Added solid CoffeeBrown border
        border = BorderStroke(1.dp, CoffeeBrown)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(Cream),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageResName.startsWith("http")) {
                    AsyncImage(
                        model = item.imageResName,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(item.name, fontFamily = BebasNeue, fontSize = 18.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text("₹${item.price}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoffeeBrown)
        }
    }
}