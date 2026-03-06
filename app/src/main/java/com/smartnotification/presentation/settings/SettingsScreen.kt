package com.smartnotification.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    onPrioritySettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Appearance
            item { SectionLabel("Appearance") }
            item {
                SettingToggleRow(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Switch between light and dark themes",
                    checked = uiState.darkMode,
                    onToggle = viewModel::toggleDarkMode
                )
            }

            // Notifications
            item { SectionLabel("Notifications") }
            item {
                SettingClickRow(
                    icon = Icons.Filled.Tune,
                    title = "Priority Settings",
                    subtitle = "Configure sound & vibration per priority",
                    onClick = onPrioritySettings
                )
            }
            item {
                SettingClickRow(
                    icon = Icons.Filled.ClearAll,
                    title = "Clear All Notifications",
                    subtitle = "Cancel and remove all scheduled notifications",
                    onClick = { showClearDialog = true },
                    tintColor = MaterialTheme.colorScheme.error
                )
            }

            // Data
            item { SectionLabel("Data") }
            item {
                SettingClickRow(
                    icon = Icons.Filled.RestartAlt,
                    title = "Reset App Data",
                    subtitle = "Clear all notifications and history",
                    onClick = { showResetDialog = true },
                    tintColor = MaterialTheme.colorScheme.error
                )
            }

            // About
            item { SectionLabel("About") }
            item {
                SettingInfoRow(
                    icon = Icons.Filled.Info,
                    title = "App Version",
                    subtitle = "1.0.0"
                )
            }
            item {
                SettingInfoRow(
                    icon = Icons.Filled.Code,
                    title = "Smart Notification Center",
                    subtitle = "Built with Jetpack Compose + Room + WorkManager"
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Notifications") },
            text = { Text("This will cancel and delete all scheduled notifications. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllNotifications(); showClearDialog = false }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset App Data") },
            text = { Text("This will delete ALL notifications and history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetAppData(); showResetDialog = false }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
        leadingContent = {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    )
}

@Composable
private fun SettingClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
        leadingContent = { Icon(icon, null, tint = tintColor) },
        trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
    )
}


