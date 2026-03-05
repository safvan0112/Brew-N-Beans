package com.example.coffeeshop.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.R
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    goHome: () -> Unit,
    goLogin: () -> Unit
) {

    LaunchedEffect(Unit) {

        delay(1500)

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

        // ✅ LOGO
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ APP NAME
        Text(
            text = "Brew-n-Beans",
            fontSize = 30.sp,
            color = CoffeeBrown
        )

        Spacer(modifier = Modifier.height(30.dp))

        // ✅ LOADING
        CircularProgressIndicator(
            color = CoffeeBrown,
            strokeWidth = 3.dp
        )
    }
}