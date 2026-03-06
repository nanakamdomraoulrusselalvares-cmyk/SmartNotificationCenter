package com.smartnotification.data.repository

import com.smartnotification.data.local.dao.HistoryDao
import com.smartnotification.data.local.dao.NotificationDao
import com.smartnotification.data.local.dao.PrioritySettingsDao
import com.smartnotification.data.local.*
import com.smartnotification.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val historyDao: HistoryDao,
    private val prioritySettingsDao: PrioritySettingsDao
) : NotificationRepository {

    override fun getAllNotifications(): Flow<List<NotificationItem>> =
        notificationDao.getAllNotifications().map { list -> list.map { it.toDomain() } }

    override fun getScheduledNotifications(): Flow<List<NotificationItem>> =
        notificationDao.getScheduledNotifications().map { list -> list.map { it.toDomain() } }

    override fun searchNotifications(query: String): Flow<List<NotificationItem>> =
        notificationDao.searchNotifications(query).map { list -> list.map { it.toDomain() } }

    override fun getNotificationsByPriority(priority: Priority): Flow<List<NotificationItem>> =
        notificationDao.getNotificationsByPriority(priority.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getNotificationById(id: Int): NotificationItem? =
        notificationDao.getNotificationById(id)?.toDomain()

    override suspend fun insertNotification(item: NotificationItem): Long =
        notificationDao.insertNotification(item.toEntity())

    override suspend fun updateNotification(item: NotificationItem) =
        notificationDao.updateNotification(item.toEntity())

    override suspend fun deleteNotification(item: NotificationItem) =
        notificationDao.deleteNotification(item.toEntity())

    override suspend fun deleteAllNotifications() = notificationDao.deleteAllNotifications()

    override suspend fun updateStatus(id: Int, status: NotificationStatus) =
        notificationDao.updateStatus(id, status.name)

    override suspend fun updateWorkerId(id: Int, workerId: String) =
        notificationDao.updateWorkerId(id, workerId)

    override fun getAllHistory(): Flow<List<HistoryItem>> =
        historyDao.getAllHistory().map { list -> list.map { it.toDomain() } }

    override fun searchHistory(query: String): Flow<List<HistoryItem>> =
        historyDao.searchHistory(query).map { list -> list.map { it.toDomain() } }

    override suspend fun insertHistory(item: HistoryItem) =
        historyDao.insertHistory(item.toEntity())

    override suspend fun clearHistory() = historyDao.clearHistory()

    override fun getAllPrioritySettings(): Flow<List<PrioritySettings>> =
        prioritySettingsDao.getAllSettings().map { list -> list.map { it.toDomain() } }

    override suspend fun getPrioritySettings(priority: Priority): PrioritySettings {
        return prioritySettingsDao.getSettingByPriority(priority.name)?.toDomain()
            ?: PrioritySettings(
                priority = priority,
                soundEnabled = true,
                vibrationEnabled = priority != Priority.LOW,
                headsUpEnabled = priority == Priority.HIGH
            )
    }

    override suspend fun updatePrioritySettings(settings: PrioritySettings) =
        prioritySettingsDao.insertOrUpdate(settings.toEntity())

    override suspend fun initDefaultPrioritySettings() {
        val count = prioritySettingsDao.count()
        if (count == 0) {
            Priority.values().forEach { priority ->
                prioritySettingsDao.insertOrUpdate(
                    PrioritySettings(
                        priority = priority,
                        soundEnabled = true,
                        vibrationEnabled = priority != Priority.LOW,
                        headsUpEnabled = priority == Priority.HIGH
                    ).toEntity()
                )
            }
        }
    }
}
