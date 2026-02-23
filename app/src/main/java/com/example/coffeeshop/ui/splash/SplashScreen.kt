package com.example.coffeeshop.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(
    goHome: () -> Unit,
    goLogin: () -> Unit
) {

    LaunchedEffect(Unit) {

        delay(1500) // splash delay

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null)
            goHome()
        else
            goLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Brew-N-Beans ☕",
            fontSize = 34.sp,
            color = CoffeeBrown
        )

        Spacer(Modifier.height(24.dp))

        CircularProgressIndicator(color = CoffeeBrown)
    }
}