package com.example.coffeeshop.ui.menu

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
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
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _menuItems = mutableStateOf<List<Product>>(emptyList())
    val menuItems: State<List<Product>> = _menuItems

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

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
            // COFFEE
            Product("", "Americano", "Rich, full-bodied espresso with hot water.", "15 kcal", 180, "Coffee", "americano"),
            Product("", "Caffè Mocha", "Espresso with bittersweet mocha sauce and steamed milk.", "250 kcal", 220, "Coffee", "caffe_mocha"),
            Product("", "Cappuccino", "Dark espresso under a smoothed layer of thick milk foam.", "120 kcal", 200, "Coffee", "cappuccino"),
            Product("", "Caramel Macchiato", "Freshly steamed milk with vanilla-flavored syrup marked with espresso.", "250 kcal", 240, "Coffee", "caramel_macchiato"),
            Product("", "Espresso", "Smooth signature espresso roast with rich flavor and caramelly sweetness.", "5 kcal", 150, "Coffee", "espresso"),
            Product("", "Flat White", "Smooth ristretto shots of espresso get the perfect amount of steamed whole milk.", "170 kcal", 210, "Coffee", "flat_white"),
            Product("", "Iced Caffè Latte", "Dark, rich espresso combined with milk and served over ice.", "130 kcal", 230, "Coffee", "iced_caffe_latte"),
            Product("", "Java Chip Frappe", "Mocha sauce and Frappuccino chips blended with coffee, milk and ice.", "470 kcal", 280, "Coffee", "java_chip_frappe"),
            Product("", "Latte", "Dark, rich espresso balanced with steamed milk and a light layer of foam.", "190 kcal", 200, "Coffee", "latte"),
            Product("", "Vanilla Latte", "Espresso balanced with vanilla syrup and steamed milk.", "250 kcal", 240, "Coffee", "vanilla_latte"),

            // SANDWICHES
            Product("", "Avocado Salsa Sandwich", "Fresh avocado mash with zesty salsa in multigrain bread.", "280 kcal", 210, "Sandwiches", "avocado_salsa_sandwich"),
            Product("", "Basil Tomato Sandwich", "Fresh tomatoes, mozzarella, and basil pesto on rustic bread.", "310 kcal", 190, "Sandwiches", "basil_tomato_sandwich"),
            Product("", "Chicken Tikka Sandwich", "Spicy chicken tikka chunks nestled in toasted artisan bread.", "350 kcal", 220, "Sandwiches", "chicken_tikka_sandwich"),
            Product("", "Chilli Cheese Toast", "Melted cheese with green chillies on toasted white bread.", "260 kcal", 170, "Sandwiches", "chilli_cheese_toast"),
            Product("", "Paneer Tikka Sandwich", "Spiced paneer cubes with mint chutney in a grilled sandwich.", "340 kcal", 200, "Sandwiches", "paneer_tikka_sandwich"),

            // CROISSANTS
            Product("", "Butter Croissant", "Flaky, buttery, and baked to golden perfection.", "240 kcal", 160, "Croissants", "butter_croissant"),
            Product("", "Egg White Chicken Croissant", "Flaky croissant packed with egg whites and chicken slice.", "320 kcal", 230, "Croissants", "egg_white_chicken_croissant"),
            Product("", "Tofu Spinach Croissant", "Savory croissant filled with seasoned tofu and fresh spinach.", "290 kcal", 210, "Croissants", "tofu_spinach_croissant"),
            Product("", "Vanilla Ice Cream Croissant", "Warm buttery croissant served with a scoop of vanilla bean ice cream.", "420 kcal", 250, "Croissants", "vanilla_ice_cream_croissant"),

            // DESSERTS
            Product("", "Blueberry Muffin", "Soft and fluffy muffin bursting with fresh blueberries.", "380 kcal", 150, "Desserts", "blueberry_muffin"),
            Product("", "Choco Kunafa Pastry", "Middle Eastern fusion dessert with crispy phyllo and rich chocolate.", "460 kcal", 260, "Desserts", "choco_kunafa_pastry"),
            Product("", "Chocolate Truffle Pastry", "Intensely rich chocolate sponge cake layered with dark chocolate ganache.", "410 kcal", 220, "Desserts", "chocolate_truffle_pastry"),
            Product("", "Double Choco Chip Cookie", "Chewy, decadent chocolate cookie loaded with chocolate chips.", "350 kcal", 120, "Desserts", "double_chocolate_chip_cookie"),
            Product("", "Fudgy Brownie", "Dense, gooey chocolate brownie with decadent chocolate chunks.", "410 kcal", 180, "Desserts", "fudgy_brownie"),
            Product("", "New York Cheesecake", "Classic, creamy New York style cheesecake on a graham cracker crust.", "450 kcal", 280, "Desserts", "new_york_cheesecake"),
            Product("", "Saffron Rose Loaf Cake", "Delicate sponge cake infused with aromatic saffron and rose water.", "340 kcal", 240, "Desserts", "saffron_rose_loaf_cake")
        )

        defaultMenu.forEach { repository.addProduct(it) }
        _menuItems.value = repository.getAllProducts()
    }
}