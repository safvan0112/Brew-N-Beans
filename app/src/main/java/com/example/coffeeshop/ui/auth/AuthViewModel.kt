package com.example.coffeeshop.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var error = mutableStateOf<String?>(null)
    var successMessage = mutableStateOf<String?>(null)
    var resetMessage = mutableStateOf<String?>(null)

    fun login(
        email: String,
        pass: String,
        success: () -> Unit
    ) {
        error.value = null

        repo.login(
            email,
            pass,
            onSuccess = {

                val uid = com.google.firebase.auth.FirebaseAuth
                    .getInstance()
                    .currentUser?.uid ?: return@login

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->

                        val active = doc.getBoolean("active") ?: true

                        if (active) {
                            success()
                        } else {
                            repo.logout()
                            error.value = "Account disabled by admin"
                        }
                    }
                    .addOnFailureListener {
                        error.value = "Failed to verify account"
                    }
            },
            onError = {
                error.value = "Invalid email or password"
            }
        )
    }

    fun signup(
        email: String,
        pass: String,
        success: () -> Unit
    ) {
        error.value = null

        repo.signup(
            email,
            pass,
            onSuccess = {
                successMessage.value = "Account created successfully 🎉"
                success()
            },
            onError = { error.value = it }
        )
    }

    fun resetPassword(email:String){
        if(email.isBlank()){
            resetMessage.value = "Enter email"
            return
        }

        repo.resetPassword(email){
            resetMessage.value = it
        }
    }
}