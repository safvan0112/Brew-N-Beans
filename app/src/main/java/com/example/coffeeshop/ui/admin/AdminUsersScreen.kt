package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.google.firebase.firestore.FirebaseFirestore

data class UserModel(
    val uid:String="",
    val email:String="",
    val role:String="user",
    val active:Boolean=true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    goBack:()->Unit
){

    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<UserModel>>(emptyList()) }

    LaunchedEffect(Unit){
        db.collection("users")
            .get()
            .addOnSuccessListener {
                users = it.documents.map { doc ->
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
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                navigationIcon = {
                    TextButton(onClick = goBack){
                        Text("Back", color = CoffeeBrown)
                    }
                }
            )
        }
    ){padding->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){

            items(users){ user ->

                UserCard(user, db)
            }
        }
    }
}

@Composable
private fun UserCard(user: UserModel, db: FirebaseFirestore){

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE8E0D8),
                shape = RoundedCornerShape(20.dp)
            )
    ){

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                user.email,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3E2723)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleChip(user.role)
                StatusChip(user.active)
            }

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){

                Text(
                    "Access",
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                Switch(
                    checked = user.active,
                    onCheckedChange = { state ->
                        db.collection("users")
                            .document(user.uid)
                            .update("active", state)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CoffeeBrown,
                        checkedTrackColor = CoffeeBrown.copy(.35f)
                    )
                )
            }
        }
    }
}

@Composable
private fun RoleChip(role:String){

    val bg =
        if(role=="admin") Color(0xFFEADFD6)
        else Color(0xFFF1F1F1)

    Surface(
        color = bg,
        shape = RoundedCornerShape(50)
    ){
        Text(
            role.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color(0xFF4E342E),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatusChip(active:Boolean){

    val bg =
        if(active) Color(0xFFDDEEDC)
        else Color(0xFFFFE0E0)

    val textColor =
        if(active) Color(0xFF2E7D32)
        else Color(0xFFC62828)

    Surface(
        color = bg,
        shape = RoundedCornerShape(50)
    ){
        Text(
            if(active) "ACTIVE" else "DISABLED",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}