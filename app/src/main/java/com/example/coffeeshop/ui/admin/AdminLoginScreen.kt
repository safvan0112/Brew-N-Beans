package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminLoginScreen(
    goSignup:()->Unit,
    success:()->Unit
){

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Text("Admin Login")

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(email,{email=it},label={Text("Admin Email")})
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(pass,{pass=it},label={Text("Password")})

        Spacer(Modifier.height(20.dp))

        Button(onClick = {

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email,pass)
                .addOnSuccessListener {

                    if(email.endsWith("@admin.com"))
                        success()
                    else
                        error="Not admin account"
                }
                .addOnFailureListener{
                    error=it.message ?: "Error"
                }

        }) {
            Text("Login")
        }

        TextButton(onClick=goSignup){
            Text("Create Admin")
        }

        if(error.isNotEmpty())
            Text(error,color=MaterialTheme.colorScheme.error)
    }
}