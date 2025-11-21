package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.ui.utils.loadTintedVector

@Composable
fun IconPicker(
    icons: List<String>,
    selectedIconName: String?,
    onSelect: (String) -> Unit,
    tintColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Icon", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Collapse" else "Expand All")
            }
        }

        AnimatedVisibility(visible = !expanded) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(icons) { iconName ->
                    SelectableIcon(
                        iconName = iconName,
                        selected = selectedIconName == iconName,
                        tintColor = tintColor,
                        onClick = { onSelect(iconName) },
                        tintable = iconName.contains("mono")
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
            ) {
                items(icons) { iconName ->
                    SelectableIcon(
                        iconName = iconName,
                        selected = selectedIconName == iconName,
                        tintColor = tintColor,
                        onClick = { onSelect(iconName) },
                        iconName.contains("mono")
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableIcon(
    iconName: String,
    selected: Boolean,
    tintColor: Color,
    onClick: () -> Unit,
    tintable: Boolean = true // true = monochrome, false = multicolor
) {
    val context = LocalContext.current

    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (tintable) {
            // Monochrome → tint globally
            val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
            if (resId != 0) {
                Icon(
                    painter = painterResource(resId),
                    contentDescription = iconName,
                    tint = if (tintColor.alpha == 0f) Color.Unspecified else tintColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            // Multicolor → tint dominant color only
            val vector = remember(tintColor, iconName) {
                loadTintedVector(context, iconName, tintColor)
            }

            if (vector != null) {
                Icon(
                    painter = rememberVectorPainter(vector),
                    contentDescription = iconName,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // fallback: original drawable
                val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                if (resId != 0) {
                    Icon(
                        painter = painterResource(resId),
                        contentDescription = iconName,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }



        }

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
    }
}


@Composable
fun TintedIcon(iconName: String, tintColor: Color) {
    val context = LocalContext.current
    val vector = remember { loadTintedVector(context, iconName, tintColor) }

    if (vector != null) {
        Icon(
            painter = rememberVectorPainter(vector),
            contentDescription = iconName,
            modifier = Modifier.size(32.dp)
        )
    }
}

