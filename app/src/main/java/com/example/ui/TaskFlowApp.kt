package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTaskSheet
import com.example.ui.components.PrototypeInfoSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.screens.EditTimeBlockSheet
import com.example.ui.screens.MatrixScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.QuickAssignTaskToHourSheet
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.Alabaster50
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Obsidian850
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose400
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Teal500
import com.example.util.DateTimeFormatters
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val BrandBorderBrush = Brush.linearGradient(listOf(Sky400, Teal500))

@Composable
fun TaskFlowApp(viewModel: TaskFlowViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

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

    BackHandler(enabled = uiState.currentTab != MainTab.MATRIX) {
        viewModel.selectTab(MainTab.MATRIX)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWideHeader = maxWidth >= 680.dp
        val isExpandedGrid = maxWidth >= 640.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TaskFlowHeaderBar(
                    currentTab = uiState.currentTab,
                    isWideHeader = isWideHeader,
                    isPomodoroRunning = uiState.pomodoro.isRunning,
                    remainingSecondsFlow = viewModel.remainingSeconds,
                    notificationsEnabled = notificationsEnabled,
                    onSelectTab = viewModel::selectTab,
                    onOpenAddTask = { viewModel.openAddTaskSheet(true) },
                    onOpenNotificationDialog = { showNotificationDialog = true },
                    onOpenInfo = { viewModel.openPrototypeInfoSheet(true) },
                    onOpenSettings = { viewModel.openSettingsSheet(true) }
                )
            },
            floatingActionButton = {
                if (!isWideHeader && uiState.currentTab == MainTab.MATRIX) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openAddTaskSheet(true) },
                        shape = RoundedCornerShape(18.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        text = { Text("Tugas Baru", fontWeight = FontWeight.Bold) },
                        containerColor = if (isDark) Sky400 else Sky600,
                        contentColor = if (isDark) Obsidian950 else PureWhite,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("fab_add_task")
                    )
                }
            },
            bottomBar = {
                if (!isWideHeader) {
                    TaskFlowBottomDock(
                        currentTab = uiState.currentTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                when (uiState.currentTab) {
                    MainTab.MATRIX -> {
                        MatrixScreen(
                            uiState = uiState,
                            isExpandedWidth = isExpandedGrid,
                            onSelectFilter = viewModel::setTaskFilter,
                            onSelectQuadrantFilter = viewModel::setQuadrantFilter,
                            onOpenPrototypeInfo = { viewModel.openPrototypeInfoSheet(true) },
                            onToggleComplete = viewModel::toggleTaskCompleted,
                            onMoveToFocus = { task -> viewModel.moveTaskToFocus(task, switchTab = true) },
                            onQuickSchedule = viewModel::openQuickScheduleForTask,
                            onDeleteTask = viewModel::deleteTask,
                            modifier = Modifier.widthIn(max = 1040.dp)
                        )
                    }

                    MainTab.POMODORO -> {
                        PomodoroScreen(
                            uiState = uiState,
                            remainingSecondsFlow = viewModel.remainingSeconds,
                            isWideStudio = isWideHeader,
                            onSwitchMode = viewModel::switchPomodoroMode,
                            onSetFocusMinutes = viewModel::setFocusPresetMinutes,
                            onToggleRunning = viewModel::toggleTimerRunning,
                            onResetTimer = viewModel::resetTimer,
                            onSelectTask = viewModel::selectPomodoroTask,
                            onToggleSelectedTaskDone = viewModel::toggleTaskCompleted,
                            onToggleSound = viewModel::toggleSoundEnabled,
                            onTestChime = viewModel::testChimeSound,
                            onDismissCompletionModal = viewModel::dismissCompletionModal,
                            modifier = Modifier.widthIn(max = 1040.dp)
                        )
                    }

                    MainTab.SCHEDULE -> {
                        ScheduleScreen(
                            uiState = uiState,
                            onEditHourSlot = viewModel::openEditHourSlot,
                            onToggleBlockCompleted = viewModel::toggleTimeBlockCompleted,
                            onClearBlock = viewModel::clearTimeBlock,
                            modifier = Modifier.widthIn(max = 860.dp)
                        )
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

    if (uiState.showSettingsSheet) {
        SettingsSheet(
            currentThemeMode = uiState.themeMode,
            soundEnabled = uiState.pomodoro.soundEnabled,
            notificationsEnabled = notificationsEnabled,
            onSelectThemeMode = viewModel::setThemeMode,
            onToggleSound = viewModel::toggleSoundEnabled,
            onRequestNotifications = {
                viewModel.openSettingsSheet(false)
                showNotificationDialog = true
            },
            onDismiss = { viewModel.openSettingsSheet(false) }
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
            containerColor = if (isDark) Obsidian900 else PureWhite,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = if (isDark) Sky400 else Sky600
                )
            },
            title = {
                Text("Aktifkan Izin Notifikasi", fontWeight = FontWeight.Bold)
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

@Composable
private fun TaskFlowHeaderBar(
    currentTab: MainTab,
    isWideHeader: Boolean,
    isPomodoroRunning: Boolean,
    remainingSecondsFlow: StateFlow<Int>,
    notificationsEnabled: Boolean,
    onSelectTab: (MainTab) -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenNotificationDialog: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val headerBg = if (isDark) Obsidian950.copy(alpha = 0.92f) else Alabaster50.copy(alpha = 0.92f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBg)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Identity (Gradient-bordered Rounded Badge + Title)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandBorderBrush)
                            .padding(1.2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (isDark) Obsidian900 else PureWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconTint = if (isDark) Sky400 else Sky600
                            Canvas(modifier = Modifier.size(19.dp)) {
                                val gap = size.width * 0.14f
                                val cell = (size.width - gap) / 2f
                                val radius = CornerRadius(cell * 0.30f, cell * 0.30f)
                                drawRoundRect(iconTint, Offset(0f, 0f), Size(cell, cell), radius)
                                drawRoundRect(iconTint.copy(alpha = 0.45f), Offset(cell + gap, 0f), Size(cell, cell), radius)
                                drawRoundRect(iconTint.copy(alpha = 0.45f), Offset(0f, cell + gap), Size(cell, cell), radius)
                                drawRoundRect(iconTint.copy(alpha = 0.80f), Offset(cell + gap, cell + gap), Size(cell, cell), radius)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TaskFlow Mahasiswa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isWideHeader) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Emerald500.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.22f))
                                ) {
                                    Text(
                                        text = "v2.0",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Emerald300 else Emerald700,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Tenang • Terstruktur • Amanah",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Emerald400.copy(alpha = 0.9f) else Emerald700.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                    }
                }

                // Inline Desktop / Tablet Pill Navigation
                if (isWideHeader) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Obsidian850 else Slate200.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MainTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) {
                                        if (isDark) Obsidian900 else PureWhite
                                    } else {
                                        Color.Transparent
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectTab(tab) }
                                        .testTag("rail_tab_${tab.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = null,
                                            tint = if (selected) {
                                                if (isDark) Sky400 else Sky600
                                            } else {
                                                if (isDark) Slate400 else Slate600
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (selected) {
                                                if (isDark) Sky400 else Sky600
                                            } else {
                                                if (isDark) Slate400 else Slate600
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Utility Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isWideHeader) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Sky400 else Sky600,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenAddTask)
                                .testTag("header_add_task_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = if (isDark) Obsidian950 else PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Tugas Baru",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Obsidian950 else PureWhite
                                )
                            }
                        }
                    }

                    if (isPomodoroRunning && currentTab != MainTab.POMODORO) {
                        RunningTimerPill(
                            remainingSecondsFlow = remainingSecondsFlow,
                            isDark = isDark,
                            onClick = { onSelectTab(MainTab.POMODORO) }
                        )
                    }

                    if (!notificationsEnabled) {
                        IconButton(
                            onClick = onOpenNotificationDialog,
                            modifier = Modifier.testTag("notifications_disabled_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsOff,
                                contentDescription = "Izin notifikasi nonaktif, ketuk untuk mengaktifkan",
                                tint = Rose400
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenInfo,
                        modifier = Modifier.testTag("top_bar_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Skema Alur & Info Kelompok 7",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Pengaturan Tema Light & Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun RunningTimerPill(
    remainingSecondsFlow: StateFlow<Int>,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val remainingSeconds by remainingSecondsFlow.collectAsStateWithLifecycle()
    val formattedTime = DateTimeFormatters.formatTimerSeconds(remainingSeconds)

    Surface(
        shape = RoundedCornerShape(50),
        color = Sky400.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Sky400.copy(alpha = 0.30f)),
        onClick = onClick,
        modifier = Modifier.testTag("running_timer_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = "Timer aktif",
                tint = if (isDark) Sky400 else Sky600,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formattedTime,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Sky400 else Sky600
            )
        }
    }
}

@Composable
private fun TaskFlowBottomDock(
    currentTab: MainTab,
    onSelectTab: (MainTab) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dockBg = if (isDark) Obsidian900.copy(alpha = 0.95f) else PureWhite.copy(alpha = 0.95f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(dockBg)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainTab.entries.forEach { tab ->
                val selected = currentTab == tab
                val activeColor = if (isDark) Emerald300 else Emerald700
                val inactiveColor = if (isDark) Slate400 else Slate500

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) Emerald500.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .padding(horizontal = 18.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tabIcon(tab, selected),
                            contentDescription = tab.label,
                            tint = if (selected) activeColor else inactiveColor,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected) activeColor else inactiveColor
                    )
                }
            }
        }
    }
}

private fun tabIcon(tab: MainTab, selected: Boolean) = when (tab) {
    MainTab.MATRIX -> if (selected) Icons.Rounded.GridView else Icons.Outlined.GridView
    MainTab.POMODORO -> if (selected) Icons.Rounded.Timer else Icons.Outlined.Timer
    MainTab.SCHEDULE -> if (selected) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
}
