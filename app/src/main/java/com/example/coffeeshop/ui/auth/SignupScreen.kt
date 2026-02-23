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
fun SignupScreen(
    goLogin: () -> Unit,
    success: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val error = vm.error.value
    val successMsg = vm.successMessage.value

    // Snackbar success
    LaunchedEffect(successMsg) {
        successMsg?.let {
            snackbarHostState.showSnackbar(it)
            success()
        }
    }

    // Password strength
    val strength = when {
        pass.length < 6 -> "Weak"
        pass.length < 10 -> "Medium"
        else -> "Strong"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Create Account ☕",
                fontSize = 30.sp,
                color = CoffeeBrown
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Join the coffee family",
                color = Color.Gray
            )

            Spacer(Modifier.height(40.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
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
                leadingIcon = { Icon(Icons.Default.Lock, null) },
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

            // Strength Indicator
            Text(
                text = "Strength: $strength",
                color = when (strength) {
                    "Weak" -> Color.Red
                    "Medium" -> Color(0xFFFFA000)
                    else -> Color(0xFF2E7D32)
                },
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))

            // CONFIRM PASSWORD
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton({ confirmVisible = !confirmVisible }) {
                        Icon(
                            if (confirmVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation =
                    if (confirmVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(30.dp))

            // SIGNUP BUTTON
            Button(
                onClick = {

                    if (email.isBlank() || pass.isBlank() || confirm.isBlank())
                        return@Button

                    if (pass != confirm)
                        return@Button

                    loading = true

                    scope.launch {
                        vm.signup(email, pass) { }
                        delay(900)
                        loading = false
                    }
                },
                enabled = !loading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoffeeBrown
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {

                if (loading)
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                else
                    Text("Sign Up", fontSize = 16.sp)
            }

            Spacer(Modifier.height(18.dp))

            TextButton(onClick = goLogin) {
                Text("Already have account?")
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}