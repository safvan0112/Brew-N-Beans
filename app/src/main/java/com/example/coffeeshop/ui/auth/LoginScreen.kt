package com.example.coffeeshop.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    goSignup: () -> Unit,
    goForgot: () -> Unit, // ✅ added
    success: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val error = vm.error.value
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Brew-N-Beans ☕", fontSize = 32.sp, color = CoffeeBrown)

        Spacer(Modifier.height(8.dp))

        Text("Welcome back", fontSize = 16.sp, color = Color.Gray)

        Spacer(Modifier.height(40.dp))

        // EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email,null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // PASSWORD
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock,null) },
            trailingIcon = {
                IconButton({ passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        null
                    )
                }
            },
            visualTransformation =
                if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(30.dp))

        // LOGIN BUTTON
        Button(
            onClick = {
                if (email.isBlank() || pass.isBlank()) return@Button

                loading = true

                scope.launch {
                    vm.login(email, pass) {
                        loading = false
                        success()
                    }
                    delay(800)
                    loading = false
                }
            },
            enabled = !loading,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (loading)
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            else
                Text("Login", fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))

        // Forgot Password Button (NEW)
        TextButton(onClick = goForgot) {
            Text("Forgot password?")
        }

        // Signup Button
        TextButton(onClick = goSignup) {
            Text("Create Account")
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Color.Red)
        }
    }
}