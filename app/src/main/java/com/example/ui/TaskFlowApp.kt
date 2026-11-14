package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTaskSheet
import com.example.ui.components.PrototypeInfoSheet
import com.example.ui.screens.EditTimeBlockSheet
import com.example.ui.screens.MatrixScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.QuickAssignTaskToHourSheet
import com.example.ui.screens.ScheduleScreen
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowApp(viewModel: TaskFlowViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var notificationsEnabled by remember {
        mutableStateOf(NotificationHelper.areNotificationsEnabled(context))
    }
    var showNotificationDialog by rememberSaveable { mutableStateOf(false) }
    var handledLaunchNotificationCheck by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        val enabledNow = NotificationHelper.areNotificationsEnabled(context)
        notificationsEnabled = enabledNow
        if (enabledNow) {
            NotificationHelper.ensureNotificationChannel(context)
            showNotificationDialog = false
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Izin notifikasi aktif untuk pengingat sesi fokus.",
                    duration = SnackbarDuration.Short
                )
            }
        } else {
            showNotificationDialog = true
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nowEnabled = NotificationHelper.areNotificationsEnabled(context)
                val wasEnabled = notificationsEnabled
                notificationsEnabled = nowEnabled
                if (nowEnabled) {
                    NotificationHelper.ensureNotificationChannel(context)
                    showNotificationDialog = false
                } else if (wasEnabled && !nowEnabled) {
                    showNotificationDialog = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.hasRequestedNotifications) {
        val alreadyRequested = uiState.hasRequestedNotifications ?: return@LaunchedEffect
        if (!handledLaunchNotificationCheck) {
            handledLaunchNotificationCheck = true
            val enabledNow = NotificationHelper.areNotificationsEnabled(context)
            notificationsEnabled = enabledNow
            if (enabledNow) {
                NotificationHelper.ensureNotificationChannel(context)
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !alreadyRequested &&
                !NotificationHelper.hasRuntimeNotificationPermission(context)
            ) {
                viewModel.markNotificationsRequested()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                showNotificationDialog = true
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Tugas \"${event.task.title}\" dihapus",
                        actionLabel = "Urungkan",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDeleteTask(event.task)
                    }
                }
                is UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Handle Android system back when navigating between secondary tabs
    BackHandler(enabled = uiState.currentTab != MainTab.MATRIX) {
        viewModel.selectTab(MainTab.MATRIX)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isMediumOrExpanded = maxWidth >= 600.dp
        val isLargeDualPane = maxWidth >= 960.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.GridView,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TaskFlow Mahasiswa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Tenang • Terstruktur • Amanah",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    actions = {
                        if (uiState.pomodoro.isRunning && uiState.currentTab != MainTab.POMODORO) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                onClick = { viewModel.selectTab(MainTab.POMODORO) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("running_timer_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = "Timer aktif",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = uiState.pomodoro.formattedTime,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (!notificationsEnabled) {
                            IconButton(
                                onClick = { showNotificationDialog = true },
                                modifier = Modifier.testTag("notifications_disabled_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsOff,
                                    contentDescription = "Izin notifikasi nonaktif, ketuk untuk mengaktifkan",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.openPrototypeInfoSheet(true) },
                            modifier = Modifier.testTag("top_bar_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Skema Alur & Info Kelompok 7",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                if (uiState.currentTab == MainTab.MATRIX) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openAddTaskSheet(true) },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        text = { Text("Tugas Baru") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                        modifier = Modifier.testTag("fab_add_task")
                    )
                }
            },
            bottomBar = {
                if (!isMediumOrExpanded) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = uiState.currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isMediumOrExpanded) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.size(12.dp))
                        MainTab.entries.forEach { tab ->
                            val selected = uiState.currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when {
                        isLargeDualPane && uiState.currentTab == MainTab.MATRIX -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MatrixScreen(
                                    uiState = uiState,
                                    isExpandedWidth = true,
                                    onSelectFilter = viewModel::setTaskFilter,
                                    onOpenAddTask = { viewModel.openAddTaskSheet(true) },
                                    onOpenPrototypeInfo = { viewModel.openPrototypeInfoSheet(true) },
                                    onToggleComplete = viewModel::toggleTaskCompleted,
                                    onMoveToFocus = { task -> viewModel.moveTaskToFocus(task, switchTab = false) },
                                    onQuickSchedule = viewModel::openQuickScheduleForTask,
                                    onDeleteTask = viewModel::deleteTask,
                                    modifier = Modifier.weight(1.45f)
                                )
                                PomodoroScreen(
                                    uiState = uiState,
                                    onSwitchMode = viewModel::switchPomodoroMode,
                                    onSetFocusMinutes = viewModel::setFocusPresetMinutes,
                                    onToggleRunning = viewModel::toggleTimerRunning,
                                    onResetTimer = viewModel::resetTimer,
                                    onSelectTask = viewModel::selectPomodoroTask,
                                    onToggleSelectedTaskDone = viewModel::toggleTaskCompleted,
                                    onToggleSound = viewModel::toggleSoundEnabled,
                                    onDismissCompletionModal = viewModel::dismissCompletionModal,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                )
                            }
                        }

                        uiState.currentTab == MainTab.MATRIX -> {
                            MatrixScreen(
                                uiState = uiState,
                                isExpandedWidth = isMediumOrExpanded,
                                onSelectFilter = viewModel::setTaskFilter,
                                onOpenAddTask = { viewModel.openAddTaskSheet(true) },
                                onOpenPrototypeInfo = { viewModel.openPrototypeInfoSheet(true) },
                                onToggleComplete = viewModel::toggleTaskCompleted,
                                onMoveToFocus = { task -> viewModel.moveTaskToFocus(task, switchTab = true) },
                                onQuickSchedule = viewModel::openQuickScheduleForTask,
                                onDeleteTask = viewModel::deleteTask,
                                modifier = Modifier.widthIn(max = 900.dp)
                            )
                        }

                        uiState.currentTab == MainTab.POMODORO -> {
                            PomodoroScreen(
                                uiState = uiState,
                                onSwitchMode = viewModel::switchPomodoroMode,
                                onSetFocusMinutes = viewModel::setFocusPresetMinutes,
                                onToggleRunning = viewModel::toggleTimerRunning,
                                onResetTimer = viewModel::resetTimer,
                                onSelectTask = viewModel::selectPomodoroTask,
                                onToggleSelectedTaskDone = viewModel::toggleTaskCompleted,
                                onToggleSound = viewModel::toggleSoundEnabled,
                                onDismissCompletionModal = viewModel::dismissCompletionModal,
                                modifier = Modifier.widthIn(max = 640.dp)
                            )
                        }

                        uiState.currentTab == MainTab.SCHEDULE -> {
                            ScheduleScreen(
                                uiState = uiState,
                                onEditHourSlot = viewModel::openEditHourSlot,
                                onToggleBlockCompleted = viewModel::toggleTimeBlockCompleted,
                                onClearBlock = viewModel::clearTimeBlock,
                                modifier = Modifier.widthIn(max = 760.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddTaskSheet) {
        AddTaskSheet(
            onDismiss = { viewModel.openAddTaskSheet(false) },
            onSaveTask = viewModel::addTask
        )
    }

    if (uiState.showPrototypeInfoSheet) {
        PrototypeInfoSheet(
            onDismiss = { viewModel.openPrototypeInfoSheet(false) },
            onResetDemoData = viewModel::resetDemoData
        )
    }

    val scheduleTarget = uiState.scheduleTargetTask
    if (scheduleTarget != null) {
        QuickAssignTaskToHourSheet(
            task = scheduleTarget,
            occupiedHours = uiState.timeBlocksByHour.keys,
            onAssignHour = { hour -> viewModel.assignTaskToHour(hour, scheduleTarget) },
            onDismiss = { viewModel.openQuickScheduleForTask(null) }
        )
    }

    val editingHour = uiState.editingHourSlot
    if (editingHour != null) {
        EditTimeBlockSheet(
            hourStart = editingHour,
            existingBlock = uiState.timeBlocksByHour[editingHour],
            tasks = uiState.allTasks,
            onAssignTask = viewModel::assignTaskToHour,
            onSaveCustom = viewModel::saveCustomTimeBlock,
            onClearSlot = viewModel::clearTimeBlock,
            onDismiss = { viewModel.openEditHourSlot(null) }
        )
    }

    if (showNotificationDialog && !notificationsEnabled) {
        val canRequestSystemDialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasRuntimeNotificationPermission(context) &&
            NotificationHelper.shouldShowRationale(context)

        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Aktifkan Izin Notifikasi")
            },
            text = {
                Text(
                    "Izin notifikasi sedang nonaktif untuk TaskFlow. Aktifkan notifikasi agar aplikasi dapat mengingatkan saat sesi fokus Pomodoro selesai dan membantu menjaga kedisiplinan waktumu."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationDialog = false
                        if (canRequestSystemDialog) {
                            viewModel.markNotificationsRequested()
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            NotificationHelper.openAppNotificationSettings(context)
                        }
                    },
                    modifier = Modifier.testTag("confirm_enable_notifications")
                ) {
                    Text(if (canRequestSystemDialog) "Izinkan Notifikasi" else "Buka Pengaturan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNotificationDialog = false },
                    modifier = Modifier.testTag("dismiss_notification_dialog")
                ) {
                    Text("Nanti Saja")
                }
            }
        )
    }
}

private fun tabIcon(tab: MainTab, selected: Boolean) = when (tab) {
    MainTab.MATRIX -> if (selected) Icons.Rounded.GridView else Icons.Outlined.GridView
    MainTab.POMODORO -> if (selected) Icons.Rounded.Timer else Icons.Outlined.Timer
    MainTab.SCHEDULE -> if (selected) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
}
