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
import androidx.compose.material.icons.filled.Phone
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
fun SignupScreen(
    goLogin: () -> Unit,
    success: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // OTP States
    var showOtpDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("+91") }
    var otpCode by remember { mutableStateOf("") }

    val error = vm.error.value
    val successMsg = vm.successMessage.value

    LaunchedEffect(successMsg) {
        successMsg?.let {
            snackbarHostState.showSnackbar(it)
            success()
        }
    }

    val strength = when {
        pass.isEmpty() -> ""
        pass.length < 6 -> "Weak"
        pass.length < 10 -> "Medium"
        else -> "Strong"
    }

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

    // OTP Dialog UI
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = if (!vm.isOtpSent.value) "ENTER PHONE NUMBER" else "ENTER OTP",
                    fontFamily = BebasNeue,
                    fontSize = 24.sp,
                    color = CoffeeBrown
                )
            },
            text = {
                Column {
                    if (!vm.isOtpSent.value) {
                        Text("We will send a 6-digit verification code to this number.", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number", fontFamily = Montserrat) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    } else {
                        Text("Enter the 6-digit code sent to $phoneNumber", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("6-Digit Code", fontFamily = Montserrat) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!vm.isOtpSent.value) {
                            vm.sendOtp(phoneNumber, context as Activity)
                        } else {
                            vm.verifyOtp(otpCode) {
                                showOtpDialog = false
                                success()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                ) {
                    Text(if (!vm.isOtpSent.value) "SEND SMS" else "VERIFY", fontFamily = BebasNeue, color = Color.White, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("CANCEL", fontFamily = BebasNeue, color = Color.Gray, fontSize = 16.sp)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Cream
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("JOIN BREW-N-BEANS", fontFamily = BebasNeue, fontSize = 42.sp, color = CoffeeBrown)
            Spacer(Modifier.height(8.dp))
            Text("Create an account to get started.", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
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

            if (strength.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Strength: $strength",
                    fontFamily = Montserrat,
                    fontSize = 12.sp,
                    color = when (strength) {
                        "Weak" -> Color.Red
                        "Medium" -> Color(0xFFFFA000)
                        else -> Color(0xFF2E7D32)
                    },
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // CONFIRM PASSWORD
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirm Password", fontFamily = Montserrat) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = CoffeeBrown) },
                trailingIcon = {
                    IconButton({ confirmVisible = !confirmVisible }) {
                        Icon(if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = CoffeeBrown)
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeBrown, unfocusedBorderColor = Color(0xFFD6C8B8)),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    if (email.isBlank() || pass.isBlank() || confirm.isBlank()) return@Button
                    if (pass != confirm) return@Button
                    loading = true
                    scope.launch {
                        vm.signup(email, pass) { }
                        delay(900)
                        loading = false
                    }
                },
                enabled = !loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                else Text("SIGN UP", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD6C8B8))
                Text(" OR ", fontFamily = Montserrat, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD6C8B8))
            }

            Spacer(Modifier.height(24.dp))

            // GOOGLE SIGNUP BUTTON WIRED UP
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
                Text("Sign up with Google", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
            }

            Spacer(Modifier.height(16.dp))

            // OTP SIGNUP BUTTON WIRED UP
            OutlinedButton(
                onClick = { showOtpDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFD6C8B8)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = CoffeeBrown)
                Spacer(Modifier.width(12.dp))
                Text("Sign up with Phone (OTP)", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
            }

            Spacer(Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
                Text("Login", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoffeeBrown, modifier = Modifier.clickable { goLogin() })
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, fontFamily = Montserrat, color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}