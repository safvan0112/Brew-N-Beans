package com.example.coffeeshop.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.R
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    goSignup: () -> Unit,
    goForgot: () -> Unit,
    success: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val error = vm.error.value

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                vm.signInWithGoogle(token) { success() }
            }
        } catch (e: Exception) {
            vm.error.value = "Google Sign-In Failed"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BREW-N-BEANS", fontFamily = BebasNeue, fontSize = 42.sp, color = CoffeeBrown)
        Spacer(Modifier.height(8.dp))
        Text("Welcome back, we've missed you.", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(40.dp))

        // EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", fontFamily = Montserrat) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = CoffeeBrown) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown, unfocusedBorderColor = Color(0xFFD6C8B8)),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // PASSWORD
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password", fontFamily = Montserrat) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = CoffeeBrown) },
            trailingIcon = {
                IconButton({ passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = CoffeeBrown)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown, unfocusedBorderColor = Color(0xFFD6C8B8)),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("Forgot password?", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = CoffeeBrown, modifier = Modifier.clickable { goForgot() })
        }

        Spacer(Modifier.height(24.dp))

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
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            else Text("LOGIN", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD6C8B8))
            Text(" OR ", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD6C8B8))
        }

        Spacer(Modifier.height(24.dp))

        // GOOGLE LOGIN BUTTON
        OutlinedButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFD6C8B8)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.DarkGray)
            Spacer(Modifier.width(12.dp))
            Text("Continue with Google", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
        }

        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
            Text("Create Account", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoffeeBrown, modifier = Modifier.clickable { goSignup() })
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, fontFamily = Montserrat, color = Color.Red, fontSize = 12.sp)
        }
    }
}