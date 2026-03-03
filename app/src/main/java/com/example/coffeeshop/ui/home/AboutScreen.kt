package com.example.coffeeshop.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(goBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OUR STORY", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown) },
                navigationIcon = {
                    IconButton(onClick = goBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // HERO IMAGE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1497935586351-b67a49e012bf?q=80&w=1000&auto=format&fit=crop",
                    contentDescription = "Cafe Interior",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "BREW-N-BEANS",
                        fontFamily = BebasNeue,
                        fontSize = 48.sp,
                        color = Color.White
                    )
                    Text(
                        text = "A Legacy of Flavor, Poured Fresh.",
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // SECTION 1: THE ROOTS
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("WHERE IT ALL BEGAN", fontFamily = BebasNeue, fontSize = 32.sp, color = CoffeeBrown)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Our journey didn't start in a cafe. It began decades ago with a grandfather's vision and a fiery passion for hospitality in the bustling heart of Surat. He built a legendary culinary foundation, mastering the art of bringing people together through unforgettable, grand feasts. \n\nToday, driven by that same deep-rooted dedication to unmatched flavor, we've channeled generations of family expertise into something new: Brew-N-Beans.",
                    fontFamily = Montserrat,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(Modifier.height(32.dp))

            // MID-PAGE IMAGE
            AsyncImage(
                model = "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?q=80&w=1000&auto=format&fit=crop",
                contentDescription = "Coffee Beans",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(Modifier.height(32.dp))

            // SECTION 2: THE TRANSITION
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("FROM FEASTS TO THE PERFECT POUR", fontFamily = BebasNeue, fontSize = 32.sp, color = CoffeeBrown)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Transitioning from grand-scale catering to the intimate art of coffee wasn't a pivot—it was an evolution. We realized that the secret ingredient to any great meal isn't just the recipe; it's the care, the precision, and the soul poured into it.\n\nEvery espresso shot we pull and every artisanal sandwich we grill is crafted with the exact same standard of excellence that our family has been known for. We source only the finest Arabica beans and the freshest local ingredients to ensure every bite and sip is a masterpiece.",
                    fontFamily = Montserrat,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(Modifier.height(40.dp))

            // QUOTE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CoffeeBrown)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"Great coffee is like a great legacy. It takes time, patience, and a whole lot of heart to brew perfectly.\"",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 18.sp,
                        color = Cream,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // SECTION 3: THE PROMISE
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("OUR PROMISE TO YOU", fontFamily = BebasNeue, fontSize = 32.sp, color = CoffeeBrown)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Whether you are stopping by for your morning fuel, grabbing a quick lunch, or treating yourself to a decadent dessert, you are part of our extended family. \n\nWe promise to never compromise on quality, to always serve you with a smile, and to make Brew-N-Beans your favorite escape from the daily grind.",
                    fontFamily = Montserrat,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(Modifier.height(32.dp))

            // FINAL IMAGE
            AsyncImage(
                model = "https://images.unsplash.com/photo-1600093463592-8e36ae95ef56?q=80&w=1000&auto=format&fit=crop",
                contentDescription = "Latte Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            )
        }
    }
}