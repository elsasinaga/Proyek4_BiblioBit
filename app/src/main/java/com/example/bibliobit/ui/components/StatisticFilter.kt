package com.example.bibliobit.ui.components


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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliobit.ui.theme.BiblioBitTheme
import com.example.bibliobit.ui.theme.Typography
import com.example.bibliobit.ui.theme.hijau2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.putih

@Composable
fun StatisticFilterButton(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("Day", "Week", "Month", "Year")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(hijau2)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { filter ->
            StatisticFilter(
                text = filter,
                isSelected = selectedFilter == filter.lowercase(),
                onClick = { onFilterSelected(filter.lowercase()) },
                modifier = Modifier.weight(1f) // Bobot sama untuk setiap tombol
            )
        }
    }
}

@Composable
fun StatisticFilter(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = hijau5 // Warna hijau saat aktif
    val inactiveColor = hijau2 // Warna latar belakang saat tidak aktif

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor else inactiveColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) putih else hijau5,
            style = Typography.labelSmall,
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
fun GraphFilterPreview() {
    BiblioBitTheme {
        StatisticFilterButton(
            selectedFilter = "day",
            onFilterSelected = {}
        )
    }
}