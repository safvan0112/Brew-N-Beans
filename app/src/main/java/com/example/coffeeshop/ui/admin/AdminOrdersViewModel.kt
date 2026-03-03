package com.example.coffeeshop.ui.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminOrdersViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    val activeOrders = mutableStateOf<List<Order>>(emptyList())
    val completedOrders = mutableStateOf<List<Order>>(emptyList())
    val isLoading = mutableStateOf(true)

    init {
        listenToAllOrders()
    }

    private fun listenToAllOrders() {
        db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading.value = false
                    return@addSnapshotListener
                }

                val allOrders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }

                // Separate orders into logical buckets
                activeOrders.value = allOrders.filter { it.status == "Received" || it.status == "Ready" }
                completedOrders.value = allOrders.filter { it.status == "Delivered" }

                isLoading.value = false
            }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        if (orderId.isNotBlank()) {
            db.collection("orders").document(orderId)
                .update("status", newStatus)
        }
    }
}