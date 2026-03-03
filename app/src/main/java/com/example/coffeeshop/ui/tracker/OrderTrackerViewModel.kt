package com.example.coffeeshop.ui.tracker

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderTrackerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _activeOrder = mutableStateOf<Order?>(null)
    val activeOrder: State<Order?> = _activeOrder

    init {
        listenToActiveOrder()
    }

    private fun listenToActiveOrder() {
        val user = auth.currentUser ?: return

        // Listen for any active order in real-time
        db.collection("orders")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }

                // Find the most recent order that isn't delivered yet
                val active = orders.filter { it.status == "Received" || it.status == "Ready" }
                    .maxByOrNull { it.createdAt }

                _activeOrder.value = active
            }
    }

    fun completeOrder(orderId: String) {
        if (orderId.isNotBlank()) {
            db.collection("orders").document(orderId)
                .update("status", "Delivered")
        }
    }
}