package com.example.coffeeshop.ui.cart

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.model.Order
import com.example.coffeeshop.data.model.OrderItem
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Read directly from the shared repository
    val cartItems = cartRepository.cartItems
    val productCache = cartRepository.productCache

    val orderSuccess = mutableStateOf(false)
    val isPlacingOrder = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun addOne(product: Product) {
        cartRepository.addItem(product)
    }

    fun removeOne(product: Product) {
        cartRepository.removeItem(product)
    }

    fun updateInstructions(productId: String, text: String) {
        cartRepository.updateInstructions(productId, text)
    }

    fun getTotalPrice(): Int {
        var total = 0
        cartItems.forEach { (productId, pair) ->
            val product = productCache[productId]
            if (product != null) {
                total += (product.price * pair.first)
            }
        }
        return total
    }

    fun placeOrder(paymentMethod: String) {
        val user = auth.currentUser
        if (user == null) {
            errorMessage.value = "User not logged in"
            return
        }

        if (cartItems.isEmpty()) {
            errorMessage.value = "Cart is empty"
            return
        }

        isPlacingOrder.value = true

        // Convert cart items into Firestore OrderItems
        val orderItemsList = cartItems.mapNotNull { (productId, pair) ->
            val product = productCache[productId]
            if (product != null) {
                OrderItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = pair.first,
                    instructions = pair.second,
                    imageResName = product.imageResName
                )
            } else null
        }

        val newOrder = Order(
            userId = user.uid,
            userEmail = user.email ?: "Unknown",
            items = orderItemsList,
            totalPrice = getTotalPrice(),
            paymentMethod = paymentMethod,
            status = "Received",
            createdAt = System.currentTimeMillis()
        )

        // Save to Firebase
        db.collection("orders")
            .add(newOrder)
            .addOnSuccessListener { documentRef ->
                // Update the document with its generated ID
                db.collection("orders").document(documentRef.id)
                    .update("id", documentRef.id)
                    .addOnSuccessListener {
                        cartRepository.clearCart()
                        isPlacingOrder.value = false
                        orderSuccess.value = true
                    }
            }
            .addOnFailureListener {
                isPlacingOrder.value = false
                errorMessage.value = it.message ?: "Failed to place order"
            }
    }
}