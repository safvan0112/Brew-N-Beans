package com.example.coffeeshop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    goBack: () -> Unit,
    logout: () -> Unit
) {

    val user = FirebaseAuth.getInstance().currentUser

    var name by remember {
        mutableStateOf(user?.displayName ?: "")
    }

    var editing by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(CoffeeBrown),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (name.firstOrNull() ?: "U").toString(),
                    fontSize = 40.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            // Name
            if (editing) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        loading = true

                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(update)?.addOnCompleteListener {
                            loading = false
                            editing = false
                        }
                    }
                ) {
                    if (loading)
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    else
                        Text("Save")
                }

            } else {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = name.ifBlank { "No Name" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )

                    IconButton(onClick = { editing = true }) {
                        Icon(Icons.Default.Edit, null)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = user?.email ?: "",
                color = Color.Gray
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    logout()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoffeeBrown
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}