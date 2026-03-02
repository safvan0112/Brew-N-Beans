package com.example.coffeeshop.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.ui.menu.MenuViewModel
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    logout: () -> Unit,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAboutClick: () -> Unit = {}, // Default parameter prevents NavGraph build errors
    vm: MenuViewModel = hiltViewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val menuItems by vm.menuItems

    // Animation State
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Scaffold(
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        fontFamily = Montserrat,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = user?.displayName ?: "Coffee Lover",
                        fontFamily = BebasNeue,
                        fontSize = 28.sp,
                        color = CoffeeBrown
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Profile", tint = CoffeeBrown)
                }
            }

            // Animated Welcome Section
            AnimatedVisibility(
                visible = startAnimation,
                enter = slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(durationMillis = 800)
                ) + fadeIn(animationSpec = tween(durationMillis = 800))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WELCOME TO BREW-N-BEANS!",
                        fontFamily = BebasNeue,
                        fontSize = 32.sp,
                        color = CoffeeBrown,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hand-crafted coffee, fresh sandwiches, and decadent desserts made just for you.",
                        fontFamily = Montserrat,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Best Sellers Grid Section
            val bestSellers = menuItems.take(4) // Grabs the first 4 items from your Firebase Menu

            if (bestSellers.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "OUR BEST SELLERS",
                        fontFamily = BebasNeue,
                        fontSize = 24.sp,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2x2 Grid Layout
                    val chunkedItems = bestSellers.chunked(2)
                    chunkedItems.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                BestSellerCard(item = item, modifier = Modifier.weight(1f))
                            }
                            // Fill empty space if there's an odd number of items
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Redirect Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HUNGRY?",
                        fontFamily = BebasNeue,
                        fontSize = 28.sp,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your cravings, delivered right to your table. Explore our full catalog of premium items.",
                        fontFamily = Montserrat,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onMenuClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                    ) {
                        Text("EXPLORE OUR MENU", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Us Redirect Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CoffeeBrown),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WHO WE ARE",
                        fontFamily = BebasNeue,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Discover the story behind Brew-N-Beans. We believe in quality, community, and the perfect pour.",
                        fontFamily = Montserrat,
                        fontSize = 14.sp,
                        color = Cream,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAboutClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("READ OUR STORY", fontFamily = BebasNeue, fontSize = 18.sp, color = CoffeeBrown)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp)) // Bottom padding
        }
    }
}

@Composable
fun BestSellerCard(item: Product, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageResId = remember(item.imageResName) {
        context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F8F8)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontFamily = BebasNeue,
                fontSize = 18.sp,
                color = Color.Black,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₹${item.price}",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CoffeeBrown
            )
        }
    }
}