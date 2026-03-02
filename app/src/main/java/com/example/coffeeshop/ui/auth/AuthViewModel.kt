package com.example.coffeeshop.ui.auth

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var error = mutableStateOf<String?>(null)
    var successMessage = mutableStateOf<String?>(null)
    var resetMessage = mutableStateOf<String?>(null)

    // OTP State Variables
    var storedVerificationId = mutableStateOf<String?>(null)
    var isOtpSent = mutableStateOf(false)

    // --- EXISTING EMAIL LOGIC ---
    fun login(email: String, pass: String, success: () -> Unit) {
        error.value = null
        repo.login(email, pass,
            onSuccess = {
                val uid = auth.currentUser?.uid ?: return@login
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val active = doc.getBoolean("active") ?: true
                        if (active) {
                            success()
                        } else {
                            repo.logout()
                            error.value = "Account disabled by admin"
                        }
                    }
                    .addOnFailureListener { error.value = "Failed to verify account" }
            },
            onError = { error.value = "Invalid email or password" }
        )
    }

    fun signup(email: String, pass: String, success: () -> Unit) {
        error.value = null
        repo.signup(email, pass,
            onSuccess = {
                successMessage.value = "Account created successfully 🎉"
                success()
            },
            onError = { error.value = it }
        )
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            resetMessage.value = "Enter email"
            return
        }
        repo.resetPassword(email) { resetMessage.value = it }
    }

    // --- NEW: GOOGLE LOGIN LOGIC ---
    fun signInWithGoogle(idToken: String, success: () -> Unit) {
        error.value = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener

                // Save user to Firestore if they are new
                val userData = hashMapOf(
                    "email" to (user.email ?: ""),
                    "name" to (user.displayName ?: "Coffee Lover"),
                    "role" to "user",
                    "active" to true
                )

                db.collection("users").document(user.uid).set(userData)
                    .addOnSuccessListener { success() }
                    .addOnFailureListener { error.value = "Failed to setup profile" }
            }
            .addOnFailureListener { e ->
                error.value = e.localizedMessage ?: "Google Sign-In failed"
            }
    }

    // --- NEW: PHONE OTP LOGIC ---
    fun sendOtp(phoneNumber: String, activity: Activity) {
        error.value = null
        isOtpSent.value = false

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This triggers if the phone automatically reads the SMS
                    signInWithPhoneAuthCredential(credential, {})
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    error.value = e.localizedMessage ?: "Verification failed"
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // Save the ID so the user can type in the 6-digit code
                    storedVerificationId.value = verificationId
                    isOtpSent.value = true
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String, success: () -> Unit) {
        error.value = null
        val verificationId = storedVerificationId.value

        if (verificationId == null) {
            error.value = "Please request OTP first"
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential, success)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, success: () -> Unit) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener

                val userData = hashMapOf(
                    "phone" to (user.phoneNumber ?: ""),
                    "role" to "user",
                    "active" to true
                )

                db.collection("users").document(user.uid).set(userData)
                    .addOnSuccessListener { success() }
                    .addOnFailureListener { error.value = "Failed to setup profile" }
            }
            .addOnFailureListener { e ->
                error.value = e.localizedMessage ?: "Invalid OTP code"
            }
    }
}