package com.example.coffeeshop.ui.menu

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _menuItems = mutableStateOf<List<Product>>(emptyList())
    val menuItems: State<List<Product>> = _menuItems

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    val cartState = cartRepository.cartItems

    private val _favorites = mutableStateOf<Set<String>>(emptySet())
    val favorites: State<Set<String>> = _favorites

    init {
        fetchMenu()
        listenToFavorites()
    }

    private fun listenToFavorites() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _favorites.value = snapshot.documents.map { it.id }.toSet()
            }
    }

    fun toggleFavorite(product: Product) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid).collection("favorites").document(product.id)

        if (_favorites.value.contains(product.id)) {
            docRef.delete()
        } else {
            val favData = hashMapOf(
                "productId" to product.id,
                "name" to product.name,
                "price" to product.price,
                "imageResName" to product.imageResName,
                "addedAt" to System.currentTimeMillis()
            )
            docRef.set(favData)
        }
    }

    // ✅ FIXED: Simplified listener. User side strictly reads data.
    // It will NEVER seed the database to prevent accidental duplication.
    private fun fetchMenu() {
        _isLoading.value = true
        db.collection("products").whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }
                _menuItems.value = items
                _isLoading.value = false
            }
    }

    fun addToCart(product: Product) { cartRepository.addItem(product) }
    fun removeFromCart(product: Product) { cartRepository.removeItem(product) }

    fun getCartTotalItems(): Int = cartState.values.sumOf { it.first }

    fun getCartTotalPrice(): Int {
        var total = 0
        cartState.forEach { (productId, pair) ->
            val product = cartRepository.productCache[productId]
            if (product != null) total += (product.price * pair.first)
        }
        return total
    }
}