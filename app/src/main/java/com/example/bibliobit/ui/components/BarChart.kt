package com.example.bibliobit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.example.bibliobit.ui.theme.abu1
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.Typography

@Composable
fun BarChart(
    data: Map<String, Int>,
    isScrollable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOrNull() ?: 1
    val barWidth = 16.dp
    val barShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 8.dp
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (isScrollable) it.horizontalScroll(rememberScrollState()) else it }
            .padding(vertical = 16.dp),
        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(8.dp) else Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(
                            if (value == 0) 2.dp
                            else (value.toFloat() / maxValue * 100.dp).coerceAtLeast(8.dp)
                        )
                        .clip(barShape)
                        .background(if (value == 0) abu1 else hijau5)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Label
                Text(
                    text = label,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}