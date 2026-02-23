package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    goBack: () -> Unit
) {

    var notifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(20.dp)
        ) {

            SettingSwitch(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                checked = notifications,
                onChecked = { notifications = it }
            )

            SettingSwitch(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                checked = darkMode,
                onChecked = { darkMode = it }
            )

            SettingSwitch(
                icon = Icons.Default.Security,
                title = "Admin Protection",
                checked = true,
                onChecked = {}
            )
        }
    }
}

@Composable
fun SettingSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {
                Icon(icon, null, tint = CoffeeBrown)
                Spacer(Modifier.width(14.dp))
                Text(title)
            }

            Switch(
                checked = checked,
                onCheckedChange = onChecked
            )
        }
    }
}