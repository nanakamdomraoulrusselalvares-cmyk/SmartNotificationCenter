package com.smartnotification.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartnotification.domain.model.NotificationStatus
import com.smartnotification.domain.model.Priority
import com.smartnotification.presentation.theme.*

@Composable
fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val (color, icon) = when (priority) {
        Priority.HIGH -> PriorityHighColor to Icons.Filled.PriorityHigh
        Priority.MEDIUM -> PriorityMediumColor to Icons.Filled.BarChart
        Priority.LOW -> PriorityLowColor to Icons.Filled.ArrowDownward
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            // Extended spacing for the priority text
            Spacer(Modifier.width(6.dp))
            Text(
                priority.displayName.uppercase(), 
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ), 
                color = color
            )
        }
    }
}

@Composable
fun StatusChip(status: NotificationStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        NotificationStatus.SCHEDULED -> StatusScheduledColor
        NotificationStatus.TRIGGERED -> StatusTriggeredColor
        NotificationStatus.CANCELLED -> StatusCancelledColor
    }
    
    // Logic to "extend" the word SCHEDULED
    val labelText = if (status == NotificationStatus.SCHEDULED) {
        "S C H E D U L E D"
    } else {
        status.displayName.uppercase()
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Text(
            labelText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = if (status == NotificationStatus.SCHEDULED) 2.sp else 1.sp,
                fontSize = 9.sp
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EmptyStateUI(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EmptyState")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconScale"
    )
    
    val translationY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconBounce"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.graphicsLayer { this.translationY = translationY }) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(80.dp).scale(scale),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
