package com.example.coffeeshop.ui.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.coffeeshop.data.model.Product
import com.example.coffeeshop.ui.theme.BebasNeue
import com.example.coffeeshop.ui.theme.CoffeeBrown
import com.example.coffeeshop.ui.theme.Cream
import com.example.coffeeshop.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    goBack: () -> Unit,
    vm: AdminProductsViewModel = hiltViewModel()
) {
    val products by vm.products
    val activeStates by vm.productActiveStates
    val isLoading by vm.isLoading
    val context = LocalContext.current

    var showForm by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Coffee", "Sandwiches", "Croissants", "Desserts")
    val filteredProducts = if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            title = { Text("CLEAR ENTIRE MENU?", fontFamily = BebasNeue, color = Color.Red) },
            text = { Text("This will wipe all existing menu items from the database.", fontFamily = Montserrat) },
            confirmButton = {
                TextButton(onClick = { vm.clearAllProducts { showDeleteDialog = false } }) {
                    Text("CLEAR ALL", color = Color.Red, fontFamily = BebasNeue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontFamily = BebasNeue)
                }
            }
        )
    }

    Scaffold(
        containerColor = Cream,
        floatingActionButton = {
            FloatingActionButton(onClick = { productToEdit = null; showForm = true }, containerColor = CoffeeBrown, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White).clickable { goBack() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CoffeeBrown)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("MANAGE MENU", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown)
                }
                Row {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear All", tint = Color.Red)
                    }
                    IconButton(onClick = { vm.seedDefaultMenu() }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = CoffeeBrown)
                    }
                }
            }

            // ✅ FIXED: Placed clickable inside Box to constrain ripple/shadow to RoundedCornerShape
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    val bg by animateColorAsState(if (isSelected) CoffeeBrown else Color.White, label = "bg")
                    val textC by animateColorAsState(if (isSelected) Color.White else CoffeeBrown, label = "txt")

                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = if (isSelected) 4.dp else 1.dp
                    ) {
                        Box(modifier = Modifier.clickable { selectedCategory = category }) {
                            Text(
                                text = category,
                                color = textC,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CoffeeBrown)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredProducts) { product ->
                        val isActive = activeStates[product.id] ?: true
                        AdminProductCard(product, isActive, onEdit = { productToEdit = product; showForm = true }, onToggleActive = { vm.toggleProductStatus(product.id, isActive) })
                    }
                }
            }
        }

        if (showForm) {
            ProductFormSheet(
                product = productToEdit,
                isUploading = vm.isUploading.value,
                onDismiss = { showForm = false },
                onSave = { uri, id, name, desc, price, cat, cal, oldImg ->
                    vm.saveProductWithImage(uri, id, name, desc, price, cat, cal, oldImg,
                        onSuccess = { showForm = false },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun AdminProductCard(product: Product, isActive: Boolean, onEdit: () -> Unit, onToggleActive: () -> Unit) {
    val context = LocalContext.current
    val imageResId = remember(product.imageResName) { context.resources.getIdentifier(product.imageResName, "drawable", context.packageName) }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, if(isActive) CoffeeBrown.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Color.White else Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Cream)) {
                if (product.imageResName.startsWith("http")) {
                    AsyncImage(model = product.imageResName, contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else if (imageResId != 0) {
                    Image(painterResource(id = imageResId), contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontFamily = BebasNeue, fontSize = 20.sp, color = if (isActive) Color.Black else Color.Gray)
                Text(product.category, fontFamily = Montserrat, fontSize = 12.sp, color = CoffeeBrown, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("₹${product.price}", fontFamily = Montserrat, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CoffeeBrown) }
                Spacer(Modifier.height(8.dp))
                Switch(checked = isActive, onCheckedChange = { onToggleActive() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4CAF50), uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.LightGray))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormSheet(
    product: Product?,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSave: (uri: Uri?, id: String?, name: String, desc: String, price: Int, cat: String, cal: String, oldImg: String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var calories by remember { mutableStateOf(product?.calories ?: "") }
    var oldImageRes by remember { mutableStateOf(product?.imageResName ?: "") }

    val categoryOptions = listOf("Coffee", "Sandwiches", "Croissants", "Desserts")
    var category by remember { mutableStateOf(product?.category ?: categoryOptions[0]) }
    var expandedCategory by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedImageUri = uri }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Cream) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(if (product == null) "ADD NEW ITEM" else "EDIT ITEM", fontFamily = BebasNeue, fontSize = 28.sp, color = CoffeeBrown)
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, CoffeeBrown, RoundedCornerShape(12.dp))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else if (oldImageRes.startsWith("http")) {
                    AsyncImage(model = oldImageRes, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to upload photo", fontFamily = Montserrat, fontSize = 12.sp, color = CoffeeBrown)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name", fontFamily = Montserrat) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description", fontFamily = Montserrat) }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)", fontFamily = Montserrat) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category", fontFamily = Montserrat) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = CoffeeBrown,
                            unfocusedBorderColor = Color(0xFFD6C8B8)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontFamily = Montserrat) },
                                onClick = {
                                    category = option
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories (e.g. 250 kcal)", fontFamily = Montserrat) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val priceInt = price.toIntOrNull() ?: 0
                    if (name.isNotBlank() && category.isNotBlank() && priceInt > 0) {
                        onSave(selectedImageUri, product?.id, name, desc, priceInt, category, calories, oldImageRes)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                enabled = !isUploading
            ) {
                if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("SAVE ITEM", fontFamily = BebasNeue, fontSize = 20.sp, color = Color.White)
            }
        }
    }
}