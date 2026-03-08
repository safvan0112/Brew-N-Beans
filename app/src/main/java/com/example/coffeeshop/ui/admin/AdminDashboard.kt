package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    logout: () -> Unit,
    openProfile: () -> Unit,
    openSettings: () -> Unit,
    openUsers: () -> Unit,
    openOrders: () -> Unit, // ✅ New Navigation Trigger
    openProducts: () -> Unit, // ✅ New Navigation Trigger
    vm: AdminDashboardViewModel = hiltViewModel()
) {
    val admin = FirebaseAuth.getInstance().currentUser
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADMIN PANEL 👑", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = CoffeeBrown)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color.White)) {
                        DropdownMenuItem(text = { Text("Profile", fontFamily = Montserrat) }, leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = CoffeeBrown) }, onClick = { menuExpanded = false; openProfile() })
                        DropdownMenuItem(text = { Text("Settings", fontFamily = Montserrat) }, leadingIcon = { Icon(Icons.Default.Settings, null, tint = CoffeeBrown) }, onClick = { menuExpanded = false; openSettings() })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Logout", fontFamily = Montserrat, color = Color.Red) }, leadingIcon = { Icon(Icons.Default.Logout, null, tint = Color.Red) }, onClick = { menuExpanded = false; FirebaseAuth.getInstance().signOut(); logout() })
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
                .padding(20.dp)
        ) {
            Text("WELCOME ADMIN ☕", fontFamily = BebasNeue, fontSize = 36.sp, color = CoffeeBrown)
            Text(admin?.email ?: "", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(32.dp))

            Text("DASHBOARD OVERVIEW", fontFamily = BebasNeue, fontSize = 24.sp, color = Color.Black)
            Spacer(Modifier.height(16.dp))

            // Real-Time Stats Grid
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard("Total Users", vm.userCount.intValue.toString(), Modifier.weight(1f))
                AdminStatCard("Active Orders", vm.activeOrderCount.intValue.toString(), Modifier.weight(1f), isAlert = vm.activeOrderCount.intValue > 0)
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard("Total Revenue", "₹${vm.totalRevenue.intValue}", Modifier.weight(1f))
                AdminStatCard("Menu Items", vm.productCount.intValue.toString(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(40.dp))

            Text("QUICK ACTIONS", fontFamily = BebasNeue, fontSize = 24.sp, color = Color.Black)
            Spacer(Modifier.height(16.dp))

            AdminActionButton("Manage Menu & Products", openProducts)
            AdminActionButton("Live Order Queue", openOrders)
            AdminActionButton("Manage Users", openUsers)
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, modifier: Modifier = Modifier, isAlert: Boolean = false) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAlert) Color(0xFFFFF3E0) else Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontFamily = Montserrat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isAlert) Color(0xFFE65100) else Color.Gray)
            Text(value, fontFamily = BebasNeue, fontSize = 32.sp, color = if (isAlert) Color(0xFFE65100) else CoffeeBrown)
        }
    }
}

@Composable
fun AdminActionButton(text: String, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
    ) {
        Text(text, fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
    }
}