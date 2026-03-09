package com.example.coffeeshop.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import coil.compose.AsyncImage
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat

@Composable
fun ProductDetailScreen(
    productId: String,
    goBack: () -> Unit,
    goToCart: () -> Unit,
    vm: MenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val product = vm.menuItems.value.find { it.id == productId }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CoffeeBrown)
        }
        return
    }

    val imageResId = remember(product.imageResName) {
        context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
    }

    val quantity = vm.cartState[product.id]?.first ?: 0
    val isFavorite = vm.favorites.value.contains(product.id)

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL PRICE", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "₹${product.price * if (quantity > 0) quantity else 1}",
                            fontFamily = BebasNeue,
                            fontSize = 28.sp,
                            color = CoffeeBrown
                        )
                    }

                    if (quantity == 0) {
                        Button(
                            onClick = { vm.addToCart(product) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                            modifier = Modifier.height(56.dp).fillMaxWidth(0.6f)
                        ) {
                            Text("ADD TO CART", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(CoffeeBrown).height(56.dp)
                        ) {
                            Text(
                                "-", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 24.sp,
                                modifier = Modifier.clickable { vm.removeFromCart(product) }.padding(horizontal = 24.dp)
                            )
                            Text(quantity.toString(), color = Color.White, fontFamily = BebasNeue, fontSize = 20.sp)
                            Text(
                                "+", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 24.sp,
                                modifier = Modifier.clickable { vm.addToCart(product) }.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Cream)
            ) {
                if (product.imageResName.startsWith("http")) {
                    AsyncImage(model = product.imageResName, contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else if (imageResId != 0) {
                    Image(painterResource(id = imageResId), contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)).clickable { goBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown)
                    }

                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)).clickable { vm.toggleFavorite(product) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else CoffeeBrown
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontFamily = BebasNeue,
                        fontSize = 36.sp,
                        lineHeight = 38.sp, // ✅ FIXED: Added proper gap for 2-line dish names
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Cream).padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(product.calories, fontFamily = Montserrat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(text = product.category.uppercase(), fontFamily = Montserrat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoffeeBrown)

                Spacer(Modifier.height(24.dp))

                Text("DESCRIPTION", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = product.description,
                    fontFamily = Montserrat,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 22.sp
                )
            }
        }
    }
}