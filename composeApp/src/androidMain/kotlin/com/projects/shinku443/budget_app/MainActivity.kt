package com.projects.shinku443.budget_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.projects.shinku443.budget_app.ui.screens.RootScreen
import com.projects.shinku443.budget_app.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                RootScreen()
            }
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
