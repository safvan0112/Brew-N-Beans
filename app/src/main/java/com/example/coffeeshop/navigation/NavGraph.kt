package com.example.coffeeshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coffeeshop.ui.auth.LoginScreen
import com.example.coffeeshop.ui.auth.SignupScreen
import com.example.coffeeshop.ui.home.HomeScreen
import com.example.coffeeshop.ui.splash.SplashScreen
import com.example.coffeeshop.ui.auth.ForgotPasswordScreen
import com.example.coffeeshop.ui.profile.ProfileScreen // ✅ Modified
import com.example.coffeeshop.ui.menu.MenuScreen
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
        startDestination = start
    ) {

        // SPLASH
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
                goLogin = {
                    navController.navigate(Screen.Login.route){ popUpTo(0) }
                }
            )
        }

        // LOGIN
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
                                navController.navigate(Screen.AdminHome.route){ popUpTo(Screen.Login.route){ inclusive = true } }
                            } else {
                                navController.navigate(Screen.Home.route){ popUpTo(Screen.Login.route){ inclusive = true } }
                            }
                        }
                        .addOnFailureListener {
                            navController.navigate(Screen.Home.route){ popUpTo(Screen.Login.route){ inclusive = true } }
                        }
                }
            )
        }

        // FORGOT PASSWORD
        composable(Screen.Forgot.route){
            ForgotPasswordScreen(
                goBack = { navController.popBackStack() }
            )
        }

        // SIGNUP
        composable(Screen.Signup.route) {
            SignupScreen(
                goLogin = { navController.popBackStack() },
                success = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Login.route){ inclusive = true }
                    }
                }
            )
        }

        // USER HOME
        composable(Screen.Home.route) {
            HomeScreen(
                logout = {
                    navController.navigate(Screen.Login.route){ popUpTo(0) }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onMenuClick = {
                    navController.navigate(Screen.Menu.route)
                }
            )
        }

        // MENU SCREEN
        composable(Screen.Menu.route) {
            MenuScreen(
                goBack = { navController.popBackStack() },
                goToCart = { navController.navigate(Screen.Cart.route) }
            )
        }

        // CART SCREEN
        composable(Screen.Cart.route) {
            CartScreen(
                goBack = { navController.popBackStack() }
            )
        }

        // USER PROFILE
        composable(Screen.Profile.route) {
            ProfileScreen(
                goBack = { navController.popBackStack() },
                logout = {
                    navController.navigate(Screen.Login.route){ popUpTo(0) }
                },
                goToMenu = { // ✅ Added navigation event for the fallback button
                    navController.navigate(Screen.Menu.route)
                }
            )
        }

        // ADMIN LOGIN
        composable(Screen.AdminLogin.route){
            AdminLoginScreen(
                goSignup={ navController.navigate(Screen.AdminSignup.route) },
                success={ navController.navigate(Screen.AdminHome.route){ popUpTo(0) } }
            )
        }

        // ADMIN SIGNUP
        composable(Screen.AdminSignup.route){
            AdminSignupScreen(
                goLogin={ navController.popBackStack() }
            )
        }

        // ADMIN HOME
        composable(Screen.AdminHome.route){
            AdminDashboard(
                logout = { navController.navigate(Screen.Login.route){ popUpTo(0) } },
                openProfile = { navController.navigate(Screen.AdminProfile.route) },
                openSettings = { navController.navigate(Screen.AdminSettings.route) },
                openUsers = { navController.navigate(Screen.AdminUsers.route) }
            )
        }

        // ADMIN PROFILE
        composable(Screen.AdminProfile.route){
            AdminProfileScreen(
                goBack = { navController.popBackStack() },
                logout = { navController.navigate(Screen.Login.route){ popUpTo(0) } }
            )
        }

        // ADMIN SETTINGS
        composable(Screen.AdminSettings.route){
            AdminSettingsScreen(
                goBack = { navController.popBackStack() }
            )
        }

        // ADMIN USERS
        composable(Screen.AdminUsers.route){
            AdminUsersScreen(
                goBack = { navController.popBackStack() }
            )
        }
    }
}