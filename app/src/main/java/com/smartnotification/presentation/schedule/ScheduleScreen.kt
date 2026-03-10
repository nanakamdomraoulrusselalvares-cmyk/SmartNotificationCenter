package com.smartnotification.presentation.schedule

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnotification.domain.model.NotificationItem
import com.smartnotification.presentation.components.EmptyStateUI
import com.smartnotification.presentation.components.PriorityChip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onEditClick: (Int) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "My Schedule", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = uiState.isLoading to uiState.grouped.isEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "ScheduleContent"
            ) { (loading, empty) ->
                when {
                    loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                        }
                    }
                    empty -> {
                        EmptyStateUI(
                            title = "Nothing planned",
                            subtitle = "Your upcoming notifications and alarms will appear here."
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.grouped.forEach { (date, items) ->
                                item(key = "header_$date") {
                                    DateHeader(date)
                                }
                                itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                                    val state = remember { MutableTransitionState(false) }.apply { targetState = true }
                                    AnimatedVisibility(
                                        visibleState = state,
                                        enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) +
                                                slideInVertically(
                                                    initialOffsetY = { 50 },
                                                    animationSpec = tween(500, delayMillis = index * 50)
                                                ),
                                        modifier = Modifier.animateItem()
                                    ) {
                                        ScheduleItemCard(
                                            item = item,
                                            onEdit = { onEditClick(item.id) },
                                            onCancel = { viewModel.cancelItem(item) }
                                        )
                                    }
                                }
                                item { Spacer(Modifier.height(8.dp)) }
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val isToday = date == today
    val isTomorrow = date == today.plusDays(1)
    
    val dateLabel = when {
        isToday -> "Today"
        isTomorrow -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE"))
    }
    
    val fullDate = date.format(DateTimeFormatter.ofPattern("MMMM d"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = fullDate,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleItemCard(
    item: NotificationItem,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAlarm) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    item.scheduledTime.format(timeFormatter),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-1).sp
                    )
                )
                if (item.isAlarm) {
                    Icon(
                        Icons.Filled.Alarm,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                PriorityChip(item.priority)
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Filled.Close,
                    "Cancel",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
