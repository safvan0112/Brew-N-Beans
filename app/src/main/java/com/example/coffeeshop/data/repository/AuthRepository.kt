package com.example.coffeeshop.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth
) {

    private val db = FirebaseFirestore.getInstance()

    fun login(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Login failed") }
    }

    fun signup(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid
                if (uid == null) {
                    onError("User ID null")
                    return@addOnSuccessListener
                }

                val role =
                    if (email.endsWith("@admin.com"))
                        "admin"
                    else
                        "user"

                val userMap = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "role" to role,
                    "active" to true, // ✅ added field
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(uid)
                    .set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener {
                        onError(it.message ?: "Firestore save failed")
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Signup failed")
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun resetPassword(
        email: String,
        onResult: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult("Reset link sent to email")
            }
            .addOnFailureListener {
                onResult(it.message ?: "Error occurred")
            }
    }
}