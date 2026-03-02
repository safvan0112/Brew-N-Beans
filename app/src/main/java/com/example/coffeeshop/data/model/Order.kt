package com.example.coffeeshop.data.model

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Int = 0,
    val quantity: Int = 0,
    val instructions: String = "",
    val imageResName: String = ""
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Int = 0,
    val paymentMethod: String = "COD", // "COD" or "ONLINE"
    val status: String = "Received", // Can be "Received", "Ready", or "Delivered"
    val createdAt: Long = System.currentTimeMillis()
)