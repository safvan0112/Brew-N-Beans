package com.example.coffeeshop.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coffeeshop.ui.auth.LoginScreen
import com.example.coffeeshop.ui.auth.SignupScreen
import com.example.coffeeshop.ui.home.HomeScreen
import com.example.coffeeshop.ui.home.AboutScreen
import com.example.coffeeshop.ui.splash.SplashScreen
import com.example.coffeeshop.ui.auth.ForgotPasswordScreen
import com.example.coffeeshop.ui.profile.ProfileScreen
import com.example.coffeeshop.ui.menu.MenuScreen
import com.example.coffeeshop.ui.menu.ProductDetailScreen
import com.example.coffeeshop.ui.cart.CartScreen
import com.example.coffeeshop.ui.admin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavGraph(start: String = Screen.Splash.route) {

    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    NavHost(
        navController = navController,
        startDestination = start,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) }
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                goHome = {
                    val user = auth.currentUser
                    if (user == null) {
                        navController.navigate(Screen.Login.route){ popUpTo(0) }
                        return@SplashScreen
                    }

                    db.collection("users").document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role") ?: "user"
                            if (role == "admin") {
                                navController.navigate(Screen.AdminHome.route){ popUpTo(0) }
                            } else {
                                navController.navigate(Screen.Home.route){ popUpTo(0) }
                            }
                        }
                        .addOnFailureListener {
                            navController.navigate(Screen.Home.route){ popUpTo(0) }
                        }
                },
                goLogin = { navController.navigate(Screen.Login.route){ popUpTo(0) } }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                goSignup = { navController.navigate(Screen.Signup.route) },
                goForgot = { navController.navigate(Screen.Forgot.route) },
                success = {
                    val user = auth.currentUser ?: return@LoginScreen
                    db.collection("users").document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role") ?: "user"
                            if (role == "admin") {
                                navController.navigate(Screen.AdminHome.route){ popUpTo(0) }
                            } else {
                                navController.navigate(Screen.Home.route){ popUpTo(0) }
                            }
                        }
                        .addOnFailureListener { navController.navigate(Screen.Home.route){ popUpTo(0) } }
                }
            )
        }

        composable(Screen.Forgot.route){
            ForgotPasswordScreen(goBack = { navController.popBackStack() })
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                goLogin = { navController.popBackStack() },
                success = { navController.navigate(Screen.Home.route){ popUpTo(0) } }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                logout = { navController.navigate(Screen.Login.route){ popUpTo(0) } },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onMenuClick = { navController.navigate(Screen.Menu.route) },
                onAboutClick = { navController.navigate("about_screen") },
                onProductClick = { productId -> navController.navigate("product_detail/$productId") }
            )
        }

        composable(Screen.Menu.route) {
            MenuScreen(
                goBack = { navController.popBackStack() },
                goToCart = { navController.navigate(Screen.Cart.route) },
                goToProduct = { productId -> navController.navigate("product_detail/$productId") }
            )
        }

        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                goBack = { navController.popBackStack() },
                goToCart = { navController.navigate(Screen.Cart.route) }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                goBack = { navController.popBackStack() },
                goHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
            )
        }

        composable("about_screen") {
            AboutScreen(goBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                goBack = { navController.popBackStack() },
                logout = { navController.navigate(Screen.Login.route){ popUpTo(0) } },
                goToMenu = { navController.navigate(Screen.Menu.route) },
                goToAbout = { navController.navigate("about_screen") }
            )
        }

        // ================= ADMIN ROUTES =================

        composable(Screen.AdminLogin.route){
            AdminLoginScreen(
                goSignup={ navController.navigate(Screen.AdminSignup.route) },
                success={ navController.navigate(Screen.AdminHome.route){ popUpTo(0) } }
            )
        }

        composable(Screen.AdminSignup.route){
            AdminSignupScreen(goLogin={ navController.popBackStack() })
        }

        composable(Screen.AdminHome.route){
            AdminDashboard(
                logout = { navController.navigate(Screen.Login.route){ popUpTo(0) } },
                openProfile = { navController.navigate(Screen.AdminProfile.route) },
                openSettings = { navController.navigate(Screen.AdminSettings.route) },
                openUsers = { navController.navigate(Screen.AdminUsers.route) },
                openOrders = { navController.navigate("admin_orders") }, // ✅ Wired Up
                openProducts = { navController.navigate("admin_products") } // ✅ Wired Up
            )
        }

        composable(Screen.AdminProfile.route){
            AdminProfileScreen(
                goBack = { navController.popBackStack() },
                logout = { navController.navigate(Screen.Login.route){ popUpTo(0) } }
            )
        }

        composable(Screen.AdminSettings.route){
            AdminSettingsScreen(goBack = { navController.popBackStack() })
        }

        composable(Screen.AdminUsers.route){
            AdminUsersScreen(goBack = { navController.popBackStack() })
        }

        // ✅ Placeholders for the upcoming screens (avoids crashes)
        composable("admin_orders") {
            AdminOrdersScreen(
                goBack = { navController.popBackStack() }
            )
        }

        composable("admin_products") {
            AdminProductsScreen(
                goBack = { navController.popBackStack() }
            )
        }
    }
}