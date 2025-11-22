package com.projects.shinku443.budgetapp.ui.components


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.projects.shinku443.budgetapp.ui.utils.toLong

@Composable
fun ColorPicker(
    selectedColor: Long,
    onSelectColor: (Long) -> Unit
) {
    val swatches = listOf(
        0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8,
        0xFFFF7043, 0xFF4DB6AC, 0xFFAED581, 0xFF9575CD, 0xFF7986CB
    )

    var showCustomDialog by remember { mutableStateOf(false) }
    val transparentColor = 0x00000000L

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Color", style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Transparent option
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Transparent, CircleShape)
                    .border(
                        width = if (selectedColor == transparentColor) 3.dp else 1.dp,
                        color = if (selectedColor == transparentColor)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onSelectColor(transparentColor) },
                contentAlignment = Alignment.Center
            ) {
                Text("Ø")
            }

            // Predefined swatches
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

            // Custom color option
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { showCustomDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text("…")
            }
        }
    }

    if (showCustomDialog) {
        val controller = rememberColorPickerController()

        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = controller.selectedColor.value
                    onSelectColor(selected.toLong())
                    showCustomDialog = false
                }) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Pick a custom color") },
            text = {
                Column {
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .padding(10.dp),
                        controller = controller,
                        initialColor = Color(selectedColor),
//                        onColorChanged = { /* live preview if needed */ }
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(controller.selectedColor.value, CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        )
    }
}


//        CustomColorDialog(
//            onDismiss = { showCustomDialog = false },
//            onColorPicked = { color ->
//                onSelectColor(color.toLong())
//                showCustomDialog = false
//            }
//        )
//    }
//}
//
/*

@Composable
fun CustomColorDialog(
    onDismiss: () -> Unit,
    onColorPicked: (Color) -> Unit
) {
    var red by remember { mutableStateOf(128f) }
    var green by remember { mutableStateOf(128f) }
    var blue by remember { mutableStateOf(128f) }

    val previewColor = Color(red.toInt(), green.toInt(), blue.toInt())

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onColorPicked(previewColor) }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Pick a custom color") },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(previewColor, CircleShape)
                )
                Text("Red")
                Slider(value = red, onValueChange = { red = it }, valueRange = 0f..255f)
                Text("Green")
                Slider(value = green, onValueChange = { green = it }, valueRange = 0f..255f)
                Text("Blue")
                Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..255f)
            }
        }
    )
}
*/
