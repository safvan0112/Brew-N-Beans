package com.example.coffeeshop.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    logout: () -> Unit,
    onProfileClick: () -> Unit
) {

    val user = FirebaseAuth.getInstance().currentUser
    var menuExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Espresso",
        "Latte",
        "Cappuccino",
        "Cold Coffee",
        "Mocha"
    )

    val coffees = listOf(
        Coffee("Cappuccino","₹120","https://images.unsplash.com/photo-1509042239860-f550ce710b93"),
        Coffee("Latte","₹150","https://images.unsplash.com/photo-1523942839745-7848d9c3c7f0"),
        Coffee("Espresso","₹100","https://images.unsplash.com/photo-1511920170033-f8396924c348"),
        Coffee("Mocha","₹170","https://images.unsplash.com/photo-1495474472287-4d71bcdd2085")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brew-N-Beans ☕") },
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
                                Icon(Icons.Default.AccountCircle, null)
                            },
                            onClick = {
                                menuExpanded = false
                                onProfileClick()   // ✅ navigation added
                            }
                        )

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
                .background(Cream)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(10.dp))

            Text(
                "Welcome ☕",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CoffeeBrown
            )

            Text(user?.email ?: "", color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Spacer(Modifier.height(12.dp))

            LazyRow {
                items(categories) { CategoryChip(it) }
            }

            Spacer(Modifier.height(26.dp))

            Text("Popular Drinks", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Spacer(Modifier.height(12.dp))

            LazyRow {
                items(coffees) { CoffeeCard(it) }
            }
        }
    }
}

data class Coffee(
    val name:String,
    val price:String,
    val image:String
)

@Composable
fun CategoryChip(text:String){
    Surface(
        color = CoffeeBrown,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(end = 10.dp)
    ){
        Text(
            text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun CoffeeCard(coffee: Coffee){

    Card(
        modifier = Modifier
            .width(170.dp)
            .padding(end = 16.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column {

            AsyncImage(
                model = coffee.image,
                contentDescription = null,
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(12.dp)) {

                Text(coffee.name, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(4.dp))

                Text(
                    coffee.price,
                    color = CoffeeBrown,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}