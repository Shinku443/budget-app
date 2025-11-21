package com.projects.shinku443.budgetapp.ui.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.R
import org.xmlpull.v1.XmlPullParser

/**
 * Discover all drawable resource names that start with a given prefix.
 */
fun discoverCategoryIconsByPrefix(prefix: String): List<String> {
    return R.drawable::class.java.fields
        .mapNotNull { field ->
            val name = field.name
            if (name.startsWith(prefix)) name else null
        }
        .sorted()
}

/**
 * Parse a hex color string (e.g. "#FF0000") into a Compose Color.
 */
fun parseColorString(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.Black // fallback
    }
}

/**
 * Load a vector drawable, detect its dominant fill color, and tint only that color.
 */
fun loadTintedVector(
    context: Context,
    iconName: String,
    tintColor: Color
): ImageVector? {
    val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
    if (resId == 0) return null

    val parser: XmlPullParser = context.resources.getXml(resId)

    var viewportWidth = 24f
    var viewportHeight = 24f
    val paths = mutableListOf<Pair<String, Color>>()

    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
            when (parser.name) {
                "vector" -> {
                    viewportWidth = parser.getAttributeValue(null, "android:viewportWidth")?.toFloatOrNull() ?: 24f
                    viewportHeight = parser.getAttributeValue(null, "android:viewportHeight")?.toFloatOrNull() ?: 24f
                }
                "path" -> {
                    val pathData = parser.getAttributeValue(null, "android:pathData")
                    val fillColorStr = parser.getAttributeValue(null, "android:fillColor")
                    if (pathData != null && fillColorStr != null) {
                        paths.add(pathData to parseColorString(fillColorStr))
                    }
                }
            }
        }
        eventType = parser.next()
    }

    if (paths.isEmpty()) return null // fallback later

    // Find dominant color (most frequent)
    val dominantColor = paths.groupingBy { it.second }.eachCount().maxByOrNull { it.value }?.key

    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight
    ).apply {
        paths.forEachIndexed { index, (pathData, fillColor) ->
            val effectiveColor = if (
                dominantColor != null &&
                fillColor == dominantColor &&
                index == paths.indexOfFirst { it.second == dominantColor } &&
                tintColor != Color.Transparent
            ) tintColor else fillColor

            addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(effectiveColor)
            )
        }

    }.build()
}

