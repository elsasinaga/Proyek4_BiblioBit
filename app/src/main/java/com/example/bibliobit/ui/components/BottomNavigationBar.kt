package com.example.bibliobit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bibliobit.R
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.abu3
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.putih

// Sealed class untuk merepresentasikan dua jenis ikon
sealed class IconType {
    data class Vector(val imageVector: ImageVector) : IconType()
    data class Drawable(val resId: Int) : IconType()
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        Screen.Home to IconType.Vector(Icons.Default.Home),
        Screen.Add to IconType.Vector(Icons.Default.Add),
        Screen.Statistic to IconType.Drawable(R.drawable.chart),
        Screen.Library to IconType.Drawable(R.drawable.open_book),
        Screen.Profile to IconType.Vector(Icons.Default.Person)
    )

    val background = putih
    val activeColor = hijau5
    val inactiveColor = abu3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (screen, iconType) ->
            val isSelected = when (screen) {
                Screen.Library -> {
                    currentRoute == Screen.Library.route ||
                            currentRoute?.startsWith(Screen.YourReadingBook.route.split("/{")[0]) == true ||
                            currentRoute?.startsWith(Screen.YourProgressReading.route.split("/{")[0]) == true ||
                            currentRoute?.startsWith(Screen.AddReadingProgress.route.split("/{")[0]) == true ||
                            currentRoute?.startsWith(Screen.AddYourRating.route.split("/{")[0]) == true ||
                            currentRoute?.startsWith(Screen.YourFinishBook.route.split("/{")[0]) == true ||
                            currentRoute?.startsWith(Screen.YourWishlistBook.route.split("/{")[0]) == true ||
                            currentRoute?.startsWith(Screen.Notes.route.split("/{")[0]) == true // Tambahkan rute Notes
                }
                Screen.Add -> {
                    currentRoute == Screen.Add.route ||
                            currentRoute?.startsWith(Screen.BookDetail.route.split("/{")[0]) == true
                }
                else -> {
                    currentRoute == screen.route
                }
            }

            NavigationItem(
                iconType = iconType,
                label = screen.route.replaceFirstChar { it.uppercase() },
                isSelected = isSelected,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationItem(
    iconType: IconType,
    label: String,
    isSelected: Boolean,
    activeColor: androidx.compose.ui.graphics.Color,
    inactiveColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (iconType) {
            is IconType.Vector -> {
                Icon(
                    imageVector = iconType.imageVector,
                    contentDescription = label,
                    tint = if (isSelected) activeColor else inactiveColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            is IconType.Drawable -> {
                Icon(
                    painter = painterResource(id = iconType.resId),
                    contentDescription = label,
                    tint = if (isSelected) activeColor else inactiveColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = activeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
@Preview
fun BottomNavigationBarPreview() {
    val navController = rememberNavController()
    BottomNavigationBar(navController = navController, currentRoute = "library")
}