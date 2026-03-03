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
import kotlin.math.roundToInt

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    val cartItems = cartRepository.cartItems
    val productCache = cartRepository.productCache

    val orderSuccess = mutableStateOf(false)
    val isPlacingOrder = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    // NEW: Coupon and Tax State
    val appliedCoupon = mutableStateOf<String?>(null)
    val couponDiscountAmount = mutableStateOf(0)
    var isFirstOrder = true // We check this upon loading

    init {
        checkIfFirstOrder()
    }

    private fun checkIfFirstOrder() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("orders").whereEqualTo("userId", uid).limit(1).get()
            .addOnSuccessListener { documents ->
                isFirstOrder = documents.isEmpty
            }
    }

    fun addOne(product: Product) { cartRepository.addItem(product); calculateTotals() }
    fun removeOne(product: Product) { cartRepository.removeItem(product); calculateTotals() }
    fun updateInstructions(productId: String, text: String) { cartRepository.updateInstructions(productId, text) }

    // --- ADVANCED MATH LOGIC ---

    fun getSubtotal(): Int {
        var subtotal = 0
        cartItems.forEach { (productId, pair) ->
            val product = productCache[productId]
            if (product != null) {
                val qty = pair.first
                val price = product.price

                // BOGO LOGIC: Buy 1 Get 1 Free for specific items
                if (product.name == "Java Chip Frappe" || product.name == "Caramel Macchiato") {
                    val payableQty = (qty / 2) + (qty % 2)
                    subtotal += (payableQty * price)
                } else {
                    subtotal += (qty * price)
                }
            }
        }
        return subtotal
    }

    fun applyCoupon(code: String, paymentMethod: String) {
        val subtotal = getSubtotal()
        errorMessage.value = null

        when (code.uppercase()) {
            "WELCOME20" -> {
                if (isFirstOrder) { appliedCoupon.value = "WELCOME20"; couponDiscountAmount.value = (subtotal * 0.20).roundToInt() }
                else errorMessage.value = "This coupon is only for your first order."
            }
            "SAVE10" -> {
                if (subtotal > 500) { appliedCoupon.value = "SAVE10"; couponDiscountAmount.value = (subtotal * 0.10).roundToInt() }
                else errorMessage.value = "Cart total must be above ₹500."
            }
            "MEGA25" -> {
                if (subtotal > 2000) { appliedCoupon.value = "MEGA25"; couponDiscountAmount.value = (subtotal * 0.25).roundToInt() }
                else errorMessage.value = "Cart total must be above ₹2000."
            }
            "GPAY5" -> {
                if (paymentMethod == "ONLINE") { appliedCoupon.value = "GPAY5"; couponDiscountAmount.value = (subtotal * 0.05).roundToInt() }
                else errorMessage.value = "This coupon is only valid for Online Payments."
            }
            else -> {
                appliedCoupon.value = null
                couponDiscountAmount.value = 0
                errorMessage.value = "Invalid coupon code."
            }
        }
    }

    fun removeCoupon() {
        appliedCoupon.value = null
        couponDiscountAmount.value = 0
        errorMessage.value = null
    }

    // Call this whenever cart changes so invalid coupons are removed
    private fun calculateTotals() {
        if (appliedCoupon.value != null) applyCoupon(appliedCoupon.value!!, "ONLINE")
    }

    fun getTaxAmount(): Int {
        val discountedSubtotal = getSubtotal() - couponDiscountAmount.value
        val totalTaxPercent = 0.05 // 2.5% CGST + 2.5% SGST = 5%
        return (discountedSubtotal * totalTaxPercent).roundToInt()
    }

    fun getGrandTotal(): Int {
        return getSubtotal() - couponDiscountAmount.value + getTaxAmount()
    }

    // --- CHECKOUT LOGIC ---

    fun placeOrder(paymentMethod: String) {
        val user = auth.currentUser
        if (user == null) { errorMessage.value = "User not logged in"; return }
        if (cartItems.isEmpty()) { errorMessage.value = "Cart is empty"; return }

        // Re-validate GPAY5 just in case they switched to COD at the last second
        if (appliedCoupon.value == "GPAY5" && paymentMethod != "ONLINE") {
            errorMessage.value = "GPAY5 is only valid for online payments. Please remove it or change payment method."
            return
        }

        isPlacingOrder.value = true
        errorMessage.value = null

        val orderItemsList = cartItems.mapNotNull { (productId, pair) ->
            val product = productCache[productId]
            if (product != null) {
                OrderItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price, // Keep original price for records
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
            totalPrice = getGrandTotal(), // Save the Grand Total including tax/coupons
            paymentMethod = paymentMethod,
            status = "Received",
            createdAt = System.currentTimeMillis()
        )

        db.collection("orders").add(newOrder).addOnSuccessListener { documentRef ->
            db.collection("orders").document(documentRef.id).update("id", documentRef.id).addOnSuccessListener {
                cartRepository.clearCart()
                isPlacingOrder.value = false
                orderSuccess.value = true
                appliedCoupon.value = null
                couponDiscountAmount.value = 0
            }
        }.addOnFailureListener {
            isPlacingOrder.value = false
            errorMessage.value = it.message ?: "Failed to place order"
        }
    }
}