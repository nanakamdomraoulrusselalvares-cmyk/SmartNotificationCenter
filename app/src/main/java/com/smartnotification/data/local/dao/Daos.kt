package com.smartnotification.data.local.dao

import androidx.room.*
import com.smartnotification.data.local.entity.HistoryEntity
import com.smartnotification.data.local.entity.NotificationEntity
import com.smartnotification.data.local.entity.PrioritySettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY scheduledTimeMillis ASC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Int): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE status = 'SCHEDULED' ORDER BY scheduledTimeMillis ASC")
    fun getScheduledNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR message LIKE '%' || :query || '%'")
    fun searchNotifications(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE priority = :priority ORDER BY scheduledTimeMillis ASC")
    fun getNotificationsByPriority(priority: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("UPDATE notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE notifications SET workerId = :workerId WHERE id = :id")
    suspend fun updateWorkerId(id: Int, workerId: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY triggeredAtMillis DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR message LIKE '%' || :query || '%'")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

@Dao
interface PrioritySettingsDao {
    @Query("SELECT * FROM priority_settings")
    fun getAllSettings(): Flow<List<PrioritySettingsEntity>>

    @Query("SELECT * FROM priority_settings WHERE priority = :priority")
    suspend fun getSettingByPriority(priority: String): PrioritySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: PrioritySettingsEntity)

    @Query("SELECT COUNT(*) FROM priority_settings")
    suspend fun count(): Int
}
