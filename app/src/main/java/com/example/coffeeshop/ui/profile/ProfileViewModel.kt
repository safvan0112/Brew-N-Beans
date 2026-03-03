package com.example.coffeeshop.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Data class to hold favorite items
data class FavoriteItem(
    val productId: String = "",
    val name: String = "",
    val price: Int = 0,
    val imageResName: String = "",
    val addedAt: Long = 0L
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    private val _favorites = mutableStateOf<List<FavoriteItem>>(emptyList())
    val favorites: State<List<FavoriteItem>> = _favorites

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isFavLoading = mutableStateOf(true)
    val isFavLoading: State<Boolean> = _isFavLoading

    val feedbackMessage = mutableStateOf<String?>(null)

    init {
        fetchMyOrders()
        fetchFavorites()
    }

    private fun fetchMyOrders() {
        val user = auth.currentUser
        if (user == null) {
            _isLoading.value = false
            return
        }

        db.collection("orders")
            .whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val orderList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)
                }
                _orders.value = orderList
                _isLoading.value = false
            }
    }

    private fun fetchFavorites() {
        val user = auth.currentUser
        if (user == null) {
            _isFavLoading.value = false
            return
        }

        db.collection("users").document(user.uid).collection("favorites")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isFavLoading.value = false
                    return@addSnapshotListener
                }

                val favList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FavoriteItem::class.java)
                }
                _favorites.value = favList
                _isFavLoading.value = false
            }
    }

    fun submitFeedback(subject: String, message: String) {
        val user = auth.currentUser
        if (user == null) {
            feedbackMessage.value = "You must be logged in to submit feedback."
            return
        }
        if (subject.isBlank() || message.isBlank()) {
            feedbackMessage.value = "Please fill in all fields."
            return
        }

        val feedbackData = hashMapOf(
            "userId" to user.uid,
            "userEmail" to (user.email ?: "Unknown"),
            "userName" to (user.displayName ?: "Coffee Lover"),
            "subject" to subject,
            "message" to message,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("feedback").add(feedbackData)
            .addOnSuccessListener {
                feedbackMessage.value = "Thank you! Your feedback has been received."
            }
            .addOnFailureListener {
                feedbackMessage.value = "Failed to submit feedback. Please try again."
            }
    }

    fun clearFeedbackMessage() {
        feedbackMessage.value = null
    }
}