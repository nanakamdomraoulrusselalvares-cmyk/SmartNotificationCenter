package com.smartnotification.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnotification.domain.model.NotificationItem
import com.smartnotification.domain.model.NotificationStatus
import com.smartnotification.domain.model.Priority
import com.smartnotification.presentation.components.EmptyStateUI
import com.smartnotification.presentation.components.PriorityChip
import com.smartnotification.presentation.components.StatusChip
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCreateClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val infiniteTransition = rememberInfiniteTransition(label = "FABPulse")
            val fabScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "FABScale"
            )

            LargeFloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.scale(fabScale)
            ) {
                Icon(Icons.Filled.Add, "Create", modifier = Modifier.size(32.dp))
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "SmartCenter",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notification Log */ }) {
                        Icon(Icons.Filled.NotificationsNone, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search & Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )

                FilterRow(
                    selectedPriority = uiState.selectedPriority,
                    onPrioritySelected = viewModel::onPriorityFilterChange
                )
            }

            // Notifications List
            AnimatedContent(
                targetState = uiState.isLoading to uiState.notifications.isEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "DashboardList"
            ) { (loading, empty) ->
                when {
                    loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                        }
                    }
                    empty -> {
                        EmptyStateUI(
                            title = "All clear!",
                            subtitle = "No scheduled notifications or alarms. Tap + to start."
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(
                                items = uiState.notifications,
                                key = { _, item -> item.id }
                            ) { index, item ->
                                val state = remember { MutableTransitionState(false) }.apply { targetState = true }
                                AnimatedVisibility(
                                    visibleState = state,
                                    enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) +
                                            slideInVertically(
                                                initialOffsetY = { 30 },
                                                animationSpec = tween(500, delayMillis = index * 50)
                                            ),
                                    modifier = Modifier.animateItem()
                                ) {
                                    NotificationCard(
                                        item = item,
                                        onEdit = { onEditClick(item.id) },
                                        onDelete = { viewModel.deleteItem(item) },
                                        onCancel = { viewModel.cancelItem(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search notifications...") },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, null)
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FilterRow(
    selectedPriority: Priority?,
    onPrioritySelected: (Priority?) -> Unit
) {
    val priorities = remember { Priority.entries }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FilterChip(
                selected = selectedPriority == null,
                onClick = { onPrioritySelected(null) },
                label = { Text("All") },
                shape = RoundedCornerShape(12.dp)
            )
        }
        items(priorities) { priority ->
            FilterChip(
                selected = selectedPriority == priority,
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.displayName) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = if (item.isAlarm) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.scheduledTime.format(timeFormatter),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-1).sp
                        )
                    )
                    Text(
                        text = item.scheduledTime.format(dateFormatter),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isAlarm) Icons.Filled.Alarm else Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = if (item.isAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    if (item.status == NotificationStatus.SCHEDULED) {
                        DropdownMenuItem(
                            text = { Text("Cancel") },
                            leadingIcon = { Icon(Icons.Filled.Cancel, null) },
                            onClick = { showMenu = false; onCancel() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (item.message.isNotEmpty()) {
                Text(
                    item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (item.repeatMode != com.smartnotification.domain.model.RepeatMode.NONE) {
                        Icon(
                            Icons.Filled.Repeat,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    PriorityChip(item.priority)
                }
                StatusChip(item.status)
            }
        }
    }
}
