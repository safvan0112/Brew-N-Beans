package com.example.coffeeshop.data.repository

import com.example.coffeeshop.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MenuRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    // Fetches all products from the "menu" collection in Firebase
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = db.collection("menu").get().await()
            val products = mutableListOf<Product>()
            for (document in snapshot.documents) {
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    // Attach the auto-generated Firebase Document ID
                    products.add(product.copy(id = document.id))
                }
            }
            products
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Admin function: Add a new product to Firebase
    suspend fun addProduct(product: Product): Boolean {
        return try {
            db.collection("menu").add(product).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}