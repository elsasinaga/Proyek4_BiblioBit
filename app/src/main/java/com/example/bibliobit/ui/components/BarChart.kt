package com.example.bibliobit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.example.bibliobit.ui.theme.abu1
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.Typography

@Composable
fun BarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.values.maxOrNull() ?: 1).toFloat()
    var selectedBar by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val isScrollable = data.size > 7

    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (isScrollable) it.horizontalScroll(rememberScrollState()) else it }
            .padding(vertical = 16.dp),
        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(16.dp) else Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        selectedBar = if (selectedBar?.first == label) null else Pair(label, value)
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedBar?.first == label && value > 0) {
                        Tooltip(value = value)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(
                                if (value == 0) 2.dp
                                else (value / maxValue * 150.dp).coerceAtLeast(2.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
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
}

@Composable
private fun Tooltip(value: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.8f),
        contentColor = Color.White
    ) {
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}
