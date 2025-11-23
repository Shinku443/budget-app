package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.viewmodel.UiState

@Composable
fun SyncStatusBanner(uiState: UiState) {
    val (backgroundColor, text, textColor) = when (uiState) {
        is UiState.Offline -> Triple(Color.Yellow, "Offline mode: Data is saved locally.", MaterialTheme.colorScheme.onSecondaryContainer)
        is UiState.Error -> Triple(MaterialTheme.colorScheme.errorContainer, "Connectivity Issues - Offline Mode", MaterialTheme.colorScheme.onErrorContainer)
        else -> Triple(MaterialTheme.colorScheme.surface, "", Color.Transparent)
    }

    AnimatedVisibility(
        visible = uiState is UiState.Offline || uiState is UiState.Error,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = textColor)
        }
    }
}
