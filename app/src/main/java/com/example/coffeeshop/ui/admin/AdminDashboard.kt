package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.R
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    logout: () -> Unit,
    openProfile: () -> Unit,
    openSettings: () -> Unit,
    openUsers: () -> Unit
) {

    val admin = FirebaseAuth.getInstance().currentUser
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            "Admin Panel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, null)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Profile") },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, null)
                            },
                            onClick = {
                                menuExpanded = false
                                openProfile()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, null)
                            },
                            onClick = {
                                menuExpanded = false
                                openSettings()
                            }
                        )

                        Divider()

                        DropdownMenuItem(
                            text = { Text("Logout") },
                            leadingIcon = {
                                Icon(Icons.Default.Logout, null)
                            },
                            onClick = {
                                menuExpanded = false
                                FirebaseAuth.getInstance().signOut()
                                logout()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            // ADMIN GREETING
            Text(
                "Welcome Admin",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CoffeeBrown
            )

            Text(admin?.email ?: "", color = Color.Gray)

            Spacer(Modifier.height(26.dp))

            Text(
                "Dashboard Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard("Users", "124")
                AdminStatCard("Orders", "87")
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard("Revenue", "₹12k")
                AdminStatCard("Items", "36")
            }

            Spacer(Modifier.height(30.dp))

            Text(
                "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(18.dp))

            AdminActionButton("Add Product")
            AdminActionButton("Manage Orders")
            AdminActionButton("Manage Users", openUsers)
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(100.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(title, color = Color.Gray, fontSize = 13.sp)

            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = CoffeeBrown
            )
        }
    }
}

@Composable
fun AdminActionButton(
    text: String,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
    ) {
        Text(text, fontSize = 16.sp)
    }
}