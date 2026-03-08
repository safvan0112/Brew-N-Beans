package com.example.coffeeshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.coffeeshop.navigation.NavGraph
import com.example.coffeeshop.ui.theme.CoffeeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ REQUIRED: This applies the themes.xml file and removes the Action Bar!
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            CoffeeTheme {
                NavGraph()
            }
        }
    }
}