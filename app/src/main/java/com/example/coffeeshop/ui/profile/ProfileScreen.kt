package com.example.coffeeshop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.data.model.Order
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    goBack: () -> Unit,
    logout: () -> Unit,
    goToMenu: () -> Unit, // ✅ Added to handle empty cart redirection
    vm: ProfileViewModel = hiltViewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val orders by vm.orders
    val isLoading by vm.isLoading

    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var editing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MY PROFILE", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                navigationIcon = {
                    IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) }
                },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        logout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // USER INFO SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(CoffeeBrown),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (name.firstOrNull() ?: "U").toString().uppercase(),
                        fontFamily = BebasNeue,
                        fontSize = 50.sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Name & Edit Logic
                if (editing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", fontFamily = Montserrat) },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            saving = true
                            val update = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                            user?.updateProfile(update)?.addOnCompleteListener {
                                saving = false
                                editing = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("SAVE", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name.ifBlank { "Coffee Lover" },
                            fontFamily = BebasNeue,
                            fontSize = 28.sp,
                            color = CoffeeBrown
                        )
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(text = user?.email ?: "", fontFamily = Montserrat, fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            Divider(color = Color(0xFFE8E0D8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))

            // RECENT ORDERS SECTION
            Text(
                text = "RECENT ORDERS",
                fontFamily = BebasNeue,
                fontSize = 24.sp,
                color = CoffeeBrown,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CoffeeBrown)
                }
            } else if (orders.isEmpty()) {
                // FALLBACK: IF NO ORDERS
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No orders yet.", fontFamily = Montserrat, fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = goToMenu,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                        modifier = Modifier.height(50.dp).fillMaxWidth(0.8f)
                    ) {
                        Text("EXPLORE MENU", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
                    }
                }
            } else {
                // ORDER HISTORY LIST
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        OrderHistoryCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(order.createdAt))

    // Determine status color
    val statusColor = when (order.status) {
        "Received" -> Color(0xFFFFA726) // Orange
        "Ready" -> Color(0xFF42A5F5)    // Blue
        "Delivered" -> Color(0xFF66BB6A) // Green
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(dateString, fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        fontFamily = BebasNeue,
                        fontSize = 14.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Show a quick summary of items (e.g., "2x Latte, 1x Brownie")
            val itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
            Text(
                text = itemsSummary,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Payment: ${order.paymentMethod}", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                Text("Total: ₹${order.totalPrice}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoffeeBrown)
            }
        }
    }
}