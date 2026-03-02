package com.example.coffeeshop.ui.menu

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.data.repository.CartRepository
import com.example.coffeeshop.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val cartRepository: CartRepository // Inject the shared cart
) : ViewModel() {

    private val _menuItems = mutableStateOf<List<Product>>(emptyList())
    val menuItems: State<List<Product>> = _menuItems

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    // Expose the shared cart state to the UI
    val cartState = cartRepository.cartItems

    init {
        fetchMenu()
    }

    private fun fetchMenu() {
        viewModelScope.launch {
            _isLoading.value = true
            val items = repository.getAllProducts()

            if (items.isEmpty()) {
                seedDatabase()
            } else {
                _menuItems.value = items
            }
            _isLoading.value = false
        }
    }

    // Cart Functions now pass data to the shared repository
    fun addToCart(product: Product) {
        cartRepository.addItem(product)
    }

    fun removeFromCart(product: Product) {
        cartRepository.removeItem(product)
    }

    fun getCartTotalItems(): Int {
        return cartState.values.sumOf { it.first }
    }

    fun getCartTotalPrice(): Int {
        var total = 0
        cartState.forEach { (productId, pair) ->
            val product = cartRepository.productCache[productId]
            if (product != null) {
                total += (product.price * pair.first)
            }
        }
        return total
    }

    private suspend fun seedDatabase() {
        val defaultMenu = listOf(
            Product("", "Americano", "Rich, full-bodied espresso with hot water.", "15 kcal", 180, "Coffee", "americano"),
            Product("", "Cappuccino", "Dark, rich espresso lying in wait under a smoothed and stretched layer of thick milk foam.", "120 kcal", 200, "Coffee", "cappuccino"),
            Product("", "Java Chip Frappe", "Mocha sauce and Frappuccino chips blended with coffee, milk and ice.", "470 kcal", 280, "Coffee", "java_chip_frappe"),
            Product("", "Chicken Tikka Sandwich", "Spicy chicken tikka chunks nestled in toasted artisan bread.", "350 kcal", 220, "Sandwiches", "chicken_tikka_sandwich"),
            Product("", "Avocado Salsa Sandwich", "Fresh avocado mash with zesty salsa in multigrain bread.", "280 kcal", 210, "Sandwiches", "avocado_salsa_sandwich"),
            Product("", "Butter Croissant", "Flaky, buttery, and baked to golden perfection.", "240 kcal", 160, "Croissants", "butter_croissant"),
            Product("", "Fudgy Brownie", "Dense, gooey chocolate brownie with decadent chocolate chunks.", "410 kcal", 180, "Desserts", "fudgy_brownie"),
            Product("", "New York Cheesecake", "Classic, creamy New York style cheesecake on a graham cracker crust.", "450 kcal", 280, "Desserts", "new_york_cheesecake")
        )

        defaultMenu.forEach { repository.addProduct(it) }
        _menuItems.value = repository.getAllProducts()
    }
}