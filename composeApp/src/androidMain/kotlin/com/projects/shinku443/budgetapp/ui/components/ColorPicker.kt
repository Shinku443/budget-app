package com.projects.shinku443.budgetapp.ui.components


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun ColorPicker(
    selectedColor: Long,
    onSelectColor: (Long) -> Unit
) {
    val swatches = listOf(
        0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8,
        0xFFFF7043, 0xFF4DB6AC, 0xFFAED581, 0xFF9575CD, 0xFF7986CB
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Color", style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            swatches.forEach { colorLong ->
                val color = Color(colorLong)
                val borderColor by animateColorAsState(
                    if (selectedColor == colorLong) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (selectedColor == colorLong) 3.dp else 1.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                        .clickable { onSelectColor(colorLong) }
                )
            }
        }
    }
}
