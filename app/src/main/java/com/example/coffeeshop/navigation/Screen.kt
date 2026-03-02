package com.example.coffeeshop.navigation

sealed class Screen(val route: String) {

    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Menu : Screen("menu") // Menu Screen
    object Cart : Screen("cart") // ✅ NEW CART SCREEN
    object Forgot : Screen("forgot")
    object Profile : Screen("profile")
    object AdminLogin : Screen("admin_login")
    object AdminSignup : Screen("admin_signup")
    object AdminHome : Screen("admin_home")
    object AdminProfile : Screen("admin_profile")
    object AdminSettings : Screen("admin_settings")
    object AdminUsers : Screen("admin_users")

}