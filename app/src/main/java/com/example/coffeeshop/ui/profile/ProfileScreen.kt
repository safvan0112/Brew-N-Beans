package com.example.coffeeshop.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    goBack: () -> Unit,
    logout: () -> Unit,
    goToMenu: () -> Unit,
    goToAbout: () -> Unit, // ✅ NEW NAVIGATION PARAMETER
    vm: ProfileViewModel = hiltViewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val orders by vm.orders
    val favorites by vm.favorites
    val isLoading by vm.isLoading
    val feedbackMsg by vm.feedbackMessage

    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var editing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    var expandedSection by remember { mutableStateOf<String?>("ORDERS") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var feedbackSubject by remember { mutableStateOf("") }
    var feedbackBody by remember { mutableStateOf("") }

    LaunchedEffect(feedbackMsg) {
        if (feedbackMsg != null) {
            delay(3000)
            vm.clearFeedbackMessage()
            if (feedbackMsg?.contains("Thank you") == true) {
                feedbackSubject = ""
                feedbackBody = ""
                expandedSection = null
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("LOG OUT?", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
            text = { Text("Are you sure you want to log out of your account?", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()
                        logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("LOG OUT", fontFamily = BebasNeue, color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("CANCEL", fontFamily = BebasNeue, color = Color.Gray) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MY PROFILE", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                navigationIcon = { IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) } },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFD32F2F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // USER INFO HEADER
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(CoffeeBrown), contentAlignment = Alignment.Center) {
                        Text(text = (name.firstOrNull() ?: "U").toString().uppercase(), fontFamily = BebasNeue, fontSize = 50.sp, color = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))

                    if (editing) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name", fontFamily = Montserrat) }, modifier = Modifier.fillMaxWidth(0.8f), shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                saving = true
                                val update = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                                user?.updateProfile(update)?.addOnCompleteListener { saving = false; editing = false }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown), shape = RoundedCornerShape(12.dp)
                        ) {
                            if (saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("SAVE", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = name.ifBlank { "Coffee Lover" }, fontFamily = BebasNeue, fontSize = 32.sp, color = CoffeeBrown)
                            IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                        }
                        Text(text = user?.email ?: "", fontFamily = Montserrat, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ACCORDION: RECENT ORDERS
            item {
                AccordionHeader(title = "RECENT ORDERS", icon = Icons.Default.Receipt, isExpanded = expandedSection == "ORDERS") { expandedSection = if (expandedSection == "ORDERS") null else "ORDERS" }
            }
            if (expandedSection == "ORDERS") {
                if (isLoading) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CoffeeBrown) } }
                } else if (orders.isEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No orders yet.", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                } else {
                    items(orders) { order -> OrderHistoryCard(order) }
                }
            }

            // ACCORDION: FAVORITE ITEMS
            item {
                Spacer(Modifier.height(16.dp))
                AccordionHeader(title = "MY FAVORITES", icon = Icons.Default.FavoriteBorder, isExpanded = expandedSection == "FAVORITES") { expandedSection = if (expandedSection == "FAVORITES") null else "FAVORITES" }
            }
            if (expandedSection == "FAVORITES") {
                if (favorites.isEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("You haven't added any favorites yet.", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                } else {
                    items(favorites) { fav -> FavoriteItemCard(fav, goToMenu) }
                }
            }

            // ✅ NEW: REDIRECT TO ABOUT US PAGE
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clickable { goToAbout() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CoffeeBrown),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Cream, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("THE BREW-N-BEANS STORY", fontFamily = BebasNeue, fontSize = 22.sp, color = Cream, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Cream)
                    }
                }
            }

            // ACCORDION: CONTACT & FEEDBACK
            item {
                Spacer(Modifier.height(16.dp))
                AccordionHeader(title = "CONTACT & FEEDBACK", icon = Icons.Default.ChatBubbleOutline, isExpanded = expandedSection == "CONTACT") { expandedSection = if (expandedSection == "CONTACT") null else "CONTACT" }
            }
            if (expandedSection == "CONTACT") {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("WE'D LOVE TO HEAR FROM YOU", fontFamily = BebasNeue, fontSize = 24.sp, color = CoffeeBrown)
                            Spacer(Modifier.height(8.dp))
                            Text("Have a complaint, suggestion, or just want to say hi? Let us know below.", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(value = feedbackSubject, onValueChange = { feedbackSubject = it }, label = { Text("Subject", fontFamily = Montserrat) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown), singleLine = true)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(value = feedbackBody, onValueChange = { feedbackBody = it }, label = { Text("Message", fontFamily = Montserrat) }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown), maxLines = 5)
                            Spacer(Modifier.height(16.dp))

                            if (feedbackMsg != null) {
                                Text(text = feedbackMsg!!, fontFamily = Montserrat, fontSize = 12.sp, color = if (feedbackMsg!!.contains("Thank you")) Color(0xFF4CAF50) else Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                            }

                            Button(onClick = { vm.submitFeedback(feedbackSubject, feedbackBody) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)) {
                                Text("SUBMIT", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccordionHeader(title: String, icon: ImageVector, isExpanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable { onClick() }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontFamily = BebasNeue, fontSize = 22.sp, color = Color.Black, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.rotate(rotation))
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(order.createdAt))
    val statusColor = when (order.status) { "Received" -> Color(0xFFFFA726) "Ready" -> Color(0xFF42A5F5) "Delivered" -> Color(0xFF66BB6A) else -> Color.Gray }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(dateString, fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(order.status.uppercase(), fontFamily = BebasNeue, fontSize = 14.sp, color = statusColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            val itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
            Text(itemsSummary, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Payment: ${order.paymentMethod}", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                Text("Total: ₹${order.totalPrice}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoffeeBrown)
            }
        }
    }
}

@Composable
fun FavoriteItemCard(fav: FavoriteItem, goToMenu: () -> Unit) {
    val context = LocalContext.current
    val imageResId = remember(fav.imageResName) { context.resources.getIdentifier(fav.imageResName, "drawable", context.packageName) }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clickable { goToMenu() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Cream)) {
                if (imageResId != 0) Image(painterResource(id = imageResId), contentDescription = fav.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(fav.name, fontFamily = BebasNeue, fontSize = 20.sp, color = Color.Black)
                Text("₹${fav.price}", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoffeeBrown)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = "Order Now", tint = CoffeeBrown, modifier = Modifier.size(20.dp))
        }
    }
}