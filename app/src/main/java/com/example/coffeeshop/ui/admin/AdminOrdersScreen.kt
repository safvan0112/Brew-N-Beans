package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.data.model.Order
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    goBack: () -> Unit,
    vm: AdminOrdersViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ACTIVE ORDERS", "COMPLETED")

    val activeOrders by vm.activeOrders
    val completedOrders by vm.completedOrders
    val isLoading by vm.isLoading

    val currentList = if (selectedTab == 0) activeOrders else completedOrders

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("LIVE ORDER QUEUE", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                    navigationIcon = {
                        IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Cream,
                    contentColor = CoffeeBrown
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontFamily = BebasNeue, fontSize = 18.sp) }
                        )
                    }
                }
            }
        },
        containerColor = Cream
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CoffeeBrown)
            }
        } else if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTab == 0) "No active orders right now." else "No completed orders yet.",
                    fontFamily = Montserrat, color = Color.Gray, fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentList) { order ->
                    AdminOrderTicket(
                        order = order,
                        onUpdateStatus = { newStatus -> vm.updateOrderStatus(order.id, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderTicket(order: Order, onUpdateStatus: (String) -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val timeString = dateFormat.format(Date(order.createdAt))

    val statusColor = when (order.status) {
        "Received" -> Color(0xFFFFA726)
        "Ready" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: ID and Status
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("ORDER ID: ${order.id.takeLast(6).uppercase()}", fontFamily = BebasNeue, fontSize = 20.sp, color = CoffeeBrown)
                    Text(timeString, fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(order.status.uppercase(), fontFamily = BebasNeue, fontSize = 14.sp, color = statusColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Customer Details
            Text("CUSTOMER", fontFamily = Montserrat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(order.userEmail, fontFamily = Montserrat, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)

            Spacer(Modifier.height(12.dp))

            // Order Items
            Text("ITEMS", fontFamily = Montserrat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${item.quantity}x ${item.name}", fontFamily = Montserrat, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        if (item.instructions.isNotBlank()) {
                            Text("Note: ${item.instructions}", fontFamily = Montserrat, fontSize = 12.sp, color = Color(0xFFD32F2F), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Footer: Payment and Total
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("PAYMENT", fontFamily = Montserrat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(order.paymentMethod, fontFamily = Montserrat, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(order.paymentMethod == "ONLINE") Color(0xFF4CAF50) else Color(0xFFFFA726))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL", fontFamily = Montserrat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("₹${order.totalPrice}", fontFamily = BebasNeue, fontSize = 24.sp, color = CoffeeBrown)
                }
            }

            // Action Buttons
            if (order.status != "Delivered") {
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (order.status == "Received") {
                        Button(
                            onClick = { onUpdateStatus("Ready") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.LocalDining, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("MARK AS READY", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
                        }
                    } else if (order.status == "Ready") {
                        Button(
                            onClick = { onUpdateStatus("Delivered") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("COMPLETE ORDER", fontFamily = BebasNeue, fontSize = 18.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}