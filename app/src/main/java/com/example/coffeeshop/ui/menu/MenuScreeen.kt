package com.example.coffeeshop.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.ui.home.ActiveOrderTrackerBar
import com.example.coffeeshop.ui.tracker.OrderTrackerViewModel
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat

data class Category(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    goBack: () -> Unit,
    goToCart: () -> Unit,
    goToProduct: (String) -> Unit = {},
    vm: MenuViewModel = hiltViewModel(),
    trackerVm: OrderTrackerViewModel = hiltViewModel()
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val menuItems by vm.menuItems
    val isLoading by vm.isLoading
    val activeOrder by trackerVm.activeOrder

    val totalItems = vm.getCartTotalItems()
    val totalPrice = vm.getCartTotalPrice()

    val getQuantity: (String) -> Int = { productId -> vm.cartState[productId]?.first ?: 0 }

    val categories = listOf(Category("All"), Category("Coffee"), Category("Sandwiches"), Category("Croissants"), Category("Desserts"))
    val filteredMenu = if (selectedCategory == "All") menuItems else menuItems.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OUR MENU", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                navigationIcon = { IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream,
        bottomBar = {
            Column {
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

                AnimatedVisibility(
                    visible = totalItems > 0,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CoffeeBrown)
                                .clickable { goToCart() }
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("$totalItems ITEM${if(totalItems > 1) "S" else ""}", color = Color.White, fontFamily = BebasNeue, fontSize = 18.sp)
                                Text("₹$totalPrice", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("VIEW CART", color = Color.White, fontFamily = BebasNeue, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ShoppingBag, contentDescription = "Cart", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { category ->
                    CategoryPill(category, selectedCategory == category.name) { selectedCategory = category.name }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CoffeeBrown)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp), verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredMenu) { item ->
                        ModernMenuCard(
                            item = item,
                            quantity = getQuantity(item.id),
                            onAdd = { vm.addToCart(item) },
                            onRemove = { vm.removeFromCart(item) },
                            onClick = { goToProduct(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPill(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (isSelected) CoffeeBrown else Color.White, label = "bg")
    val textC by animateColorAsState(if (isSelected) Color.White else CoffeeBrown, label = "txt")
    Surface(modifier = Modifier.clickable { onClick() }, color = bg, shape = RoundedCornerShape(24.dp), shadowElevation = if (isSelected) 4.dp else 1.dp) {
        Text(category.name, color = textC, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
    }
}

@Composable
fun ModernMenuCard(item: Product, quantity: Int, onAdd: () -> Unit, onRemove: () -> Unit, onClick: () -> Unit = {}) {
    val context = LocalContext.current
    val imageResId = remember(item.imageResName) {
        context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // ✅ CHANGED: Removed the 'copy(alpha = 0.4f)' to make the border completely dark and crisp
            .border(1.dp, CoffeeBrown, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(16.dp)).background(Cream)) {
            if (item.imageResName.startsWith("http")) {
                AsyncImage(model = item.imageResName, contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else if (imageResId != 0) {
                Image(painterResource(id = imageResId), contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }

        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontFamily = BebasNeue, fontSize = 24.sp, color = Color.Black, lineHeight = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(item.description, fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = Color(0xFFE57373), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(item.calories, fontFamily = Montserrat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("₹${item.price}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CoffeeBrown)
                if (quantity == 0) {
                    Surface(onClick = onAdd, shape = RoundedCornerShape(8.dp), color = Cream, border = BorderStroke(1.dp, CoffeeBrown)) {
                        Text("ADD", fontFamily = BebasNeue, fontSize = 16.sp, color = CoffeeBrown, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(CoffeeBrown)) {
                        Text("-", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { onRemove() }.padding(horizontal = 12.dp, vertical = 4.dp))
                        Text(quantity.toString(), color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("+", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { onAdd() }.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}