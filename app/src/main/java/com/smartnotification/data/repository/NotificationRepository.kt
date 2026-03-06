package com.smartnotification.data.repository

import com.smartnotification.domain.model.*
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<NotificationItem>>
    fun getScheduledNotifications(): Flow<List<NotificationItem>>
    fun searchNotifications(query: String): Flow<List<NotificationItem>>
    fun getNotificationsByPriority(priority: Priority): Flow<List<NotificationItem>>
    suspend fun getNotificationById(id: Int): NotificationItem?
    suspend fun insertNotification(item: NotificationItem): Long
    suspend fun updateNotification(item: NotificationItem)
    suspend fun deleteNotification(item: NotificationItem)
    suspend fun deleteAllNotifications()
    suspend fun updateStatus(id: Int, status: NotificationStatus)
    suspend fun updateWorkerId(id: Int, workerId: String)

    fun getAllHistory(): Flow<List<HistoryItem>>
    fun searchHistory(query: String): Flow<List<HistoryItem>>
    suspend fun insertHistory(item: HistoryItem)
    suspend fun clearHistory()

    fun getAllPrioritySettings(): Flow<List<PrioritySettings>>
    suspend fun getPrioritySettings(priority: Priority): PrioritySettings
    suspend fun updatePrioritySettings(settings: PrioritySettings)
    suspend fun initDefaultPrioritySettings()
}
