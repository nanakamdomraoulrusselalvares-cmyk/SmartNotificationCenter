package com.smartnotification.presentation.priority

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnotification.domain.model.Priority
import com.smartnotification.domain.model.PrioritySettings
import com.smartnotification.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityScreen(
    onBack: () -> Unit,
    viewModel: PriorityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Priority Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        "Configure how each priority level behaves when a notification fires.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(uiState.settings, key = { it.priority.name }) { settings ->
                    PriorityCard(
                        settings = settings,
                        onSoundToggle = { viewModel.toggleSound(settings.priority, it) },
                        onVibrationToggle = { viewModel.toggleVibration(settings.priority, it) },
                        onHeadsUpToggle = { viewModel.toggleHeadsUp(settings.priority, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityCard(
    settings: PrioritySettings,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onHeadsUpToggle: (Boolean) -> Unit
) {
    val color = when (settings.priority) {
        Priority.HIGH -> PriorityHighColor
        Priority.MEDIUM -> PriorityMediumColor
        Priority.LOW -> PriorityLowColor
    }
    val description = when (settings.priority) {
        Priority.HIGH -> "Heads-up alert · interrupts user"
        Priority.MEDIUM -> "Standard notification in tray"
        Priority.LOW -> "Silent — no interruption"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            when (settings.priority) {
                                Priority.HIGH -> Icons.Filled.PriorityHigh
                                Priority.MEDIUM -> Icons.Filled.BarChart
                                Priority.LOW -> Icons.Filled.ArrowDownward
                            },
                            null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "${settings.priority.displayName} Priority",
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            ToggleRow("Sound", Icons.Filled.VolumeUp, settings.soundEnabled, onSoundToggle)
            ToggleRow("Vibration", Icons.Filled.Vibration, settings.vibrationEnabled, onVibrationToggle)
            ToggleRow("Heads-up", Icons.Filled.NotificationImportant, settings.headsUpEnabled, onHeadsUpToggle)
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
