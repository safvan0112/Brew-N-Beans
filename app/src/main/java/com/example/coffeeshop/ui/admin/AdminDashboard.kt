package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun AdminDashboard(
    logout: () -> Unit,
    openProfile: () -> Unit,
    openSettings: () -> Unit,
    openUsers: () -> Unit,
    openOrders: () -> Unit,
    openProducts: () -> Unit,
    vm: AdminDashboardViewModel = hiltViewModel()
) {
    val admin = FirebaseAuth.getInstance().currentUser

    Scaffold(
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ NEW: Custom Hero Header (Same as User side)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(brush = Brush.verticalGradient(colors = listOf(CoffeeBrown, Color(0xFF3E2723))))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("WELCOME BACK,", fontFamily = Montserrat, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("ADMINISTRATOR", fontFamily = BebasNeue, fontSize = 32.sp, color = Color.White)
                        }

                        // Profile Avatar (Clicking this opens Profile where Logout is)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { openProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "A", fontFamily = BebasNeue, fontSize = 26.sp, color = Color.White)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text("DASHBOARD OVERVIEW", fontFamily = BebasNeue, fontSize = 24.sp, color = Color.Black)
                Spacer(Modifier.height(16.dp))

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