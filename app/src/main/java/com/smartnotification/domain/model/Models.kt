package com.smartnotification.domain.model

import java.time.LocalDateTime

/**
 * Domain model for a scheduled notification or alarm.
 */
data class NotificationItem(
    val id: Int = 0,
    val title: String,
    val message: String,
    val scheduledTime: LocalDateTime,
    val priority: Priority,
    val repeatMode: RepeatMode,
    val status: NotificationStatus,
    val isAlarm: Boolean = false,
    val workerId: String? = null
)

/**
 * Domain model for a notification history entry.
 */
data class HistoryItem(
    val id: Int = 0,
    val notificationId: Int,
    val title: String,
    val message: String,
    val triggeredAt: LocalDateTime,
    val priority: Priority
)

/**
 * Domain model for priority settings.
 */
data class PrioritySettings(
    val priority: Priority,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val headsUpEnabled: Boolean = true
)

enum class Priority(val displayName: String, val level: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2)
}

enum class RepeatMode(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly")
}

enum class NotificationStatus(val displayName: String) {
    SCHEDULED("Scheduled"),
    TRIGGERED("Triggered"),
    CANCELLED("Cancelled")
}
