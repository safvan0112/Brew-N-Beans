package com.example.coffeeshop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminSignupScreen(
    goLogin:()->Unit
){

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Text("Admin Register")

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(email,{email=it},label={Text("Admin Email")})
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(pass,{pass=it},label={Text("Password")})

        Spacer(Modifier.height(20.dp))

        Button(onClick = {

            if(!email.endsWith("@admin.com")){
                msg="Use admin email"
                return@Button
            }

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,pass)
                .addOnSuccessListener {
                    msg="Admin created"
                }
                .addOnFailureListener{
                    msg=it.message ?: "Error"
                }

        }) {
            Text("Register")
        }

        TextButton(onClick=goLogin){
            Text("Back to login")
        }

        if(msg.isNotEmpty())
            Text(msg)
    }
}