package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PreferencesManager
import com.example.data.TaskFlowDatabase
import com.example.data.TaskFlowRepository
import com.example.data.ThemeMode
import com.example.ui.TaskFlowApp
import com.example.ui.TaskFlowViewModel
import com.example.ui.theme.TaskFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TaskFlowViewModel by viewModels {
        val db = TaskFlowDatabase.getInstance(applicationContext)
        val prefs = PreferencesManager(applicationContext)
        val repository = TaskFlowRepository(
            taskDao = db.taskDao(),
            timeBlockDao = db.timeBlockDao(),
            preferencesManager = prefs
        )
        TaskFlowViewModel.Factory(repository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            TaskFlowTheme(darkTheme = isDarkTheme) {
                TaskFlowApp(viewModel = viewModel)
            }
        }
    }
}
