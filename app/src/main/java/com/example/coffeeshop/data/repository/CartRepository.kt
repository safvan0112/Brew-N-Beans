package com.example.coffeeshop.data.repository

import androidx.compose.runtime.mutableStateMapOf
import com.example.coffeeshop.data.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {

    // Maps Product ID to a Pair containing (Quantity, Custom Instructions)
    val cartItems = mutableStateMapOf<String, Pair<Int, String>>()

    // Keep a cache of the actual Product objects so we don't have to re-fetch them
    val productCache = mutableMapOf<String, Product>()

    fun addItem(product: Product) {
        productCache[product.id] = product
        val currentData = cartItems[product.id] ?: Pair(0, "")
        cartItems[product.id] = currentData.copy(first = currentData.first + 1)
    }

    fun removeItem(product: Product) {
        val currentData = cartItems[product.id] ?: return
        if (currentData.first > 1) {
            cartItems[product.id] = currentData.copy(first = currentData.first - 1)
        } else {
            cartItems.remove(product.id)
            productCache.remove(product.id)
        }
    }

    fun updateInstructions(productId: String, instructions: String) {
        val currentData = cartItems[productId]
        if (currentData != null) {
            cartItems[productId] = currentData.copy(second = instructions)
        }
    }

    fun clearCart() {
        cartItems.clear()
        productCache.clear()
    }
}