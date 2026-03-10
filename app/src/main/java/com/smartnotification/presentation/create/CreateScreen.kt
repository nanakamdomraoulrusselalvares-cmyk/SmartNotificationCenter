package com.smartnotification.presentation.create

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnotification.domain.model.Priority
import com.smartnotification.domain.model.RepeatMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    onBack: () -> Unit,
    viewModel: CreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorShown()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (uiState.id == 0) "New Notification" else "Edit Notification",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Input Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = viewModel::onTitleChange,
                            label = { Text("Title") },
                            placeholder = { Text("What's this about?") },
                            isError = uiState.titleError != null,
                            supportingText = uiState.titleError?.let { { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = uiState.message,
                            onValueChange = viewModel::onMessageChange,
                            label = { Text("Message") },
                            placeholder = { Text("Add some details...") },
                            isError = uiState.messageError != null,
                            supportingText = uiState.messageError?.let { { Text(it) } },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // Schedule Section
                Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScheduleButton(
                        icon = Icons.Filled.CalendarToday,
                        text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    ScheduleButton(
                        icon = Icons.Filled.AccessTime,
                        text = uiState.selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Options Section
                Text("Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DropdownField(
                            label = "Priority",
                            selected = uiState.priority.displayName,
                            options = Priority.values().map { it.displayName },
                            onOptionSelected = { idx -> viewModel.onPriorityChange(Priority.values()[idx]) }
                        )

                        DropdownField(
                            label = "Repeat",
                            selected = uiState.repeatMode.displayName,
                            options = RepeatMode.values().map { it.displayName },
                            onOptionSelected = { idx -> viewModel.onRepeatModeChange(RepeatMode.values()[idx]) }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (uiState.isAlarm) Icons.Filled.AlarmOn else Icons.Filled.Alarm,
                                    contentDescription = null,
                                    tint = if (uiState.isAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Set as Alarm", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("Rings even in silent mode", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = uiState.isAlarm,
                                onCheckedChange = viewModel::onIsAlarmChange
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Actions
                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Save Notification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialDate = uiState.selectedDate,
            onDateSelected = { date ->
                viewModel.onDateChange(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerModal(
            initialTime = uiState.selectedTime,
            onTimeSelected = { time ->
                viewModel.onTimeChange(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun ScheduleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEachIndexed { idx, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(idx); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(date)
                }
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModal(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface),
        content = {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                TimePicker(state = timePickerState)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = {
                        onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    }) { Text("Confirm") }
                }
            }
        }
    )
}
