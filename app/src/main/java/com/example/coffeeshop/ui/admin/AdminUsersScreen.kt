package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import com.google.firebase.firestore.FirebaseFirestore

data class UserModel(
    val uid: String = "",
    val email: String = "",
    val role: String = "user",
    val active: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    goBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<UserModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        // ✅ FIXED: Changed to real-time SnapshotListener so toggling the switch updates the UI instantly
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            users = snapshot.documents.map { doc ->
                UserModel(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "user",
                    active = doc.getBoolean("active") ?: true
                )
            }
        }
    }

    Scaffold(
        containerColor = Cream
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ NEW: Custom Top Header matching the Admin Profile redesign
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { goBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown)
                }
                Spacer(Modifier.width(16.dp))
                Text("MANAGE USERS", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(users) { user ->
                    UserCard(user, db)
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: UserModel, db: FirebaseFirestore) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE8E0D8),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                user.email,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleChip(user.role)
                StatusChip(user.active)
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (user.active) "Revoke Access" else "Grant Access",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Switch(
                    checked = user.active,
                    onCheckedChange = { state ->
                        // This updates Firestore, and the snapshot listener above will instantly redraw the UI
                        db.collection("users")
                            .document(user.uid)
                            .update("active", state)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CoffeeBrown,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    val bg = if (role == "admin") Color(0xFFEADFD6) else Color(0xFFF1F1F1)
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            role.uppercase(),
            fontFamily = BebasNeue,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color(0xFF4E342E)
        )
    }
}

@Composable
private fun StatusChip(active: Boolean) {
    val bg = if (active) Color(0xFFDDEEDC) else Color(0xFFFFE0E0)
    val textColor = if (active) Color(0xFF2E7D32) else Color(0xFFC62828)

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            if (active) "ACTIVE" else "DISABLED",
            fontFamily = BebasNeue,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor
        )
    }
}