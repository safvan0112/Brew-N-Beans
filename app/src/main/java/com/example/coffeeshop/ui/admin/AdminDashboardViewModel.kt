package com.example.coffeeshop.ui.admin

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    val userCount = mutableIntStateOf(0)
    val activeOrderCount = mutableIntStateOf(0)
    val totalRevenue = mutableIntStateOf(0)
    val productCount = mutableIntStateOf(0)

    init {
        listenToStats()
    }

    private fun listenToStats() {
        // 1. Listen to Total Users
        db.collection("users").addSnapshotListener { snap, _ ->
            userCount.intValue = snap?.size() ?: 0
        }

        // 2. Listen to Total Products (Only active ones, supporting your "soft delete" idea)
        db.collection("products").addSnapshotListener { snap, _ ->
            if (snap != null) {
                productCount.intValue = snap.documents.count { it.getBoolean("active") != false }
            }
        }

        // 3. Listen to Orders (For Active Count & Total Revenue)
        db.collection("orders").addSnapshotListener { snap, _ ->
            if (snap != null) {
                var active = 0
                var revenue = 0

                for (doc in snap.documents) {
                    val status = doc.getString("status") ?: ""
                    if (status == "Received" || status == "Ready") {
                        active++
                    } else if (status == "Delivered") {
                        revenue += doc.getLong("totalPrice")?.toInt() ?: 0
                    }
                }

                activeOrderCount.intValue = active
                totalRevenue.intValue = revenue
            }
        }
    }
}