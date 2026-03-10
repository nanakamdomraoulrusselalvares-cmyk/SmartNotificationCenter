package com.smartnotification.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val scheduledTimeMillis: Long,
    val priority: String,       // LOW / MEDIUM / HIGH
    val repeatMode: String,     // NONE / DAILY / WEEKLY
    val status: String,         // SCHEDULED / TRIGGERED / CANCELLED
    val isAlarm: Boolean = false,
    val workerId: String? = null
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notificationId: Int,
    val title: String,
    val message: String,
    val triggeredAtMillis: Long,
    val priority: String
)

@Entity(tableName = "priority_settings")
data class PrioritySettingsEntity(
    @PrimaryKey
    val priority: String,       // LOW / MEDIUM / HIGH
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val headsUpEnabled: Boolean = true
)
