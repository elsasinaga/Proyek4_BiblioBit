package com.example.bibliobit.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliobit.R
import com.example.bibliobit.ui.theme.BiblioBitTheme
import com.example.bibliobit.ui.theme.Typography
import com.example.bibliobit.ui.theme.hijau2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.putih

sealed class FilterIcon {
    data class Vector(val imageVector: ImageVector) : FilterIcon()
    data class Drawable(@DrawableRes val resId: Int) : FilterIcon()
}

@Composable
fun FilterBar(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "all" to FilterIcon.Drawable(R.drawable.library),
        "reading" to FilterIcon.Drawable(R.drawable.reading),
        "wishlist" to FilterIcon.Drawable(R.drawable.bookmark),
        "finish" to FilterIcon.Drawable(R.drawable.trophy)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // Pastikan FilterBar memiliki tinggi yang cukup
            .shadow(4.dp, RoundedCornerShape(16.dp)) // Tambahkan shadow untuk efek mengambang
            .clip(RoundedCornerShape(16.dp))
            .background(hijau2)
            .padding(horizontal = 8.dp, vertical = 8.dp), // Pastikan padding vertikal cukup
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { (status, icon) ->
            FilterButton(
                text = status.replaceFirstChar { it.uppercase() },
                icon = icon,
                isSelected = selectedFilter == status,
                onClick = { onFilterSelected(status) }
            )
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    icon: FilterIcon, // Perbaiki tipe parameter dari ImageVector menjadi IconType
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = hijau5 // Warna hijau saat aktif
    val inactiveColor = hijau2 // Warna putih saat tidak aktif

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor else inactiveColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp), // Pastikan padding vertikal cukup
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (icon) {
            is FilterIcon.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = text,
                    tint = if (isSelected) hijau2 else hijau5,
                    modifier = Modifier.size(20.dp) // Ukuran ikon seimbang
                )
            }
            is FilterIcon.Drawable -> {
                Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = text,
                    tint = if (isSelected) hijau2 else hijau5,
                    modifier = Modifier.size(20.dp) // Ukuran ikon seimbang
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp)) // Jarak antara ikon dan teks
        Text(
            text = text,
            color = if (isSelected) hijau2 else hijau5,
            style = Typography.labelSmall,
            fontSize = 12.sp // Ukuran teks seimbang
        )
    }
}

@Preview
@Composable
fun FilterBarPreview() {
    BiblioBitTheme {
        FilterBar(
            selectedFilter = "all",
            onFilterSelected = {}
        )
    }
}