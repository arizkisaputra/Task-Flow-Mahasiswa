package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.PreferencesManager
import com.example.data.TaskFlowDatabase
import com.example.data.TaskFlowRepository
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
            TaskFlowTheme {
                TaskFlowApp(viewModel = viewModel)
            }
        }
    }
}
