package com.smartnotification.data.local

import com.smartnotification.data.local.entity.HistoryEntity
import com.smartnotification.data.local.entity.NotificationEntity
import com.smartnotification.data.local.entity.PrioritySettingsEntity
import com.smartnotification.domain.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun NotificationEntity.toDomain(): NotificationItem = NotificationItem(
    id = id,
    title = title,
    message = message,
    scheduledTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(scheduledTimeMillis), ZoneId.systemDefault()
    ),
    priority = Priority.valueOf(priority),
    repeatMode = RepeatMode.valueOf(repeatMode),
    status = NotificationStatus.valueOf(status),
    workerId = workerId
)

fun NotificationItem.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    title = title,
    message = message,
    scheduledTimeMillis = scheduledTime
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),
    priority = priority.name,
    repeatMode = repeatMode.name,
    status = status.name,
    workerId = workerId
)

fun HistoryEntity.toDomain(): HistoryItem = HistoryItem(
    id = id,
    notificationId = notificationId,
    title = title,
    message = message,
    triggeredAt = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(triggeredAtMillis), ZoneId.systemDefault()
    ),
    priority = Priority.valueOf(priority)
)

fun HistoryItem.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    notificationId = notificationId,
    title = title,
    message = message,
    triggeredAtMillis = triggeredAt
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),
    priority = priority.name
)

fun PrioritySettingsEntity.toDomain(): PrioritySettings = PrioritySettings(
    priority = Priority.valueOf(priority),
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    headsUpEnabled = headsUpEnabled
)

fun PrioritySettings.toEntity(): PrioritySettingsEntity = PrioritySettingsEntity(
    priority = priority.name,
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    headsUpEnabled = headsUpEnabled
)
