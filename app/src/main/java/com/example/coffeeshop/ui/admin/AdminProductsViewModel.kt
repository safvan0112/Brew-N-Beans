package com.example.coffeeshop.ui.admin

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminProductsViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val storage = FirebaseStorage.getInstance()

    val products = mutableStateOf<List<Product>>(emptyList())
    val productActiveStates = mutableStateOf<Map<String, Boolean>>(emptyMap())
    val isLoading = mutableStateOf(true)
    val isUploading = mutableStateOf(false)

    init { listenToInventory() }

    private fun listenToInventory() {
        db.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                isLoading.value = false
                return@addSnapshotListener
            }
            val fetchedProducts = mutableListOf<Product>()
            val activeMap = mutableMapOf<String, Boolean>()

            for (doc in snapshot.documents) {
                val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                if (product != null) {
                    fetchedProducts.add(product)
                    activeMap[doc.id] = doc.getBoolean("active") ?: true
                }
            }
            products.value = fetchedProducts
            productActiveStates.value = activeMap
            isLoading.value = false
        }
    }

    fun clearAllProducts(onSuccess: () -> Unit) {
        isLoading.value = true
        db.collection("products").get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                onSuccess()
                isLoading.value = false
            }
        }.addOnFailureListener {
            isLoading.value = false
        }
    }

    fun saveProductWithImage(
        uri: Uri?,
        id: String?,
        name: String,
        desc: String,
        price: Int,
        category: String,
        calories: String,
        oldImageRes: String,
        onSuccess: () -> Unit
    ) {
        isUploading.value = true
        if (uri != null) {
            val ref = storage.reference.child("product_images/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveToFirestore(id, name, desc, price, category, calories, downloadUrl.toString(), onSuccess)
                }
            }.addOnFailureListener { isUploading.value = false }
        } else {
            saveToFirestore(id, name, desc, price, category, calories, oldImageRes, onSuccess)
        }
    }

    private fun saveToFirestore(
        id: String?, name: String, desc: String, price: Int, category: String, calories: String, imageRes: String, onSuccess: () -> Unit
    ) {
        val productData = hashMapOf(
            "name" to name, "description" to desc, "price" to price, "category" to category,
            "calories" to calories, "imageResName" to imageRes, "active" to true
        )
        if (id.isNullOrEmpty()) {
            val newRef = db.collection("products").document()
            productData["id"] = newRef.id
            newRef.set(productData).addOnSuccessListener { isUploading.value = false; onSuccess() }
        } else {
            productData["id"] = id
            db.collection("products").document(id).update(productData as Map<String, Any>).addOnSuccessListener { isUploading.value = false; onSuccess() }
        }
    }

    fun toggleProductStatus(productId: String, currentStatus: Boolean) {
        db.collection("products").document(productId).update("active", !currentStatus)
    }

    fun seedDefaultMenu() {
        val defaultMenu = listOf(
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
            Product("", "Avocado Salsa Sandwich", "Fresh avocado mash with zesty salsa in multigrain bread.", "280 kcal", 210, "Sandwiches", "avocado_salsa_sandwich"),
            Product("", "Basil Tomato Sandwich", "Fresh tomatoes, mozzarella, and basil pesto on rustic bread.", "310 kcal", 190, "Sandwiches", "basil_tomato_sandwich"),
            Product("", "Chicken Tikka Sandwich", "Spicy chicken tikka chunks nestled in toasted artisan bread.", "350 kcal", 220, "Sandwiches", "chicken_tikka_sandwich"),
            Product("", "Chilli Cheese Toast", "Melted cheese with green chillies on toasted white bread.", "260 kcal", 170, "Sandwiches", "chilli_cheese_toast"),
            Product("", "Paneer Tikka Sandwich", "Spiced paneer cubes with mint chutney in a grilled sandwich.", "340 kcal", 200, "Sandwiches", "paneer_tikka_sandwich"),
            Product("", "Butter Croissant", "Flaky, buttery, and baked to golden perfection.", "240 kcal", 160, "Croissants", "butter_croissant"),
            Product("", "Egg White Chicken Croissant", "Flaky croissant packed with egg whites and chicken slice.", "320 kcal", 230, "Croissants", "egg_white_chicken_croissant"),
            Product("", "Tofu Spinach Croissant", "Savory croissant filled with seasoned tofu and fresh spinach.", "290 kcal", 210, "Croissants", "tofu_spinach_croissant"),
            Product("", "Vanilla Ice Cream Croissant", "Warm buttery croissant served with a scoop of vanilla bean ice cream.", "420 kcal", 250, "Croissants", "vanilla_ice_cream_croissant"),
            Product("", "Blueberry Muffin", "Soft and fluffy muffin bursting with fresh blueberries.", "380 kcal", 150, "Desserts", "blueberry_muffin"),
            Product("", "Choco Kunafa Pastry", "Middle Eastern fusion dessert with crispy phyllo and rich chocolate.", "460 kcal", 260, "Desserts", "choco_kunafa_pastry"),
            Product("", "Chocolate Truffle Pastry", "Intensely rich chocolate sponge cake layered with dark chocolate ganache.", "410 kcal", 220, "Desserts", "chocolate_truffle_pastry"),
            Product("", "Double Choco Chip Cookie", "Chewy, decadent chocolate cookie loaded with chocolate chips.", "350 kcal", 120, "Desserts", "double_chocolate_chip_cookie"),
            Product("", "Fudgy Brownie", "Dense, gooey chocolate brownie with decadent chocolate chunks.", "410 kcal", 180, "Desserts", "fudgy_brownie"),
            Product("", "New York Cheesecake", "Classic, creamy New York style cheesecake on a graham cracker crust.", "450 kcal", 280, "Desserts", "new_york_cheesecake"),
            Product("", "Saffron Rose Loaf Cake", "Delicate sponge cake infused with aromatic saffron and rose water.", "340 kcal", 240, "Desserts", "saffron_rose_loaf_cake")
        )

        // ✅ FIXED: Uses a strictly fixed Document ID so duplicates can NEVER happen
        defaultMenu.forEach { product ->
            val fixedDocId = "default_${product.imageResName}"
            val ref = db.collection("products").document(fixedDocId)
            val productData = hashMapOf(
                "id" to fixedDocId, "name" to product.name, "description" to product.description,
                "price" to product.price, "category" to product.category, "calories" to product.calories,
                "imageResName" to product.imageResName, "active" to true
            )
            ref.set(productData)
        }
    }
}