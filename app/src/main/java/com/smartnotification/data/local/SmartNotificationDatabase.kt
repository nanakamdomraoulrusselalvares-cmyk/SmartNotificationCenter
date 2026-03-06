package com.smartnotification.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartnotification.data.local.dao.HistoryDao
import com.smartnotification.data.local.dao.NotificationDao
import com.smartnotification.data.local.dao.PrioritySettingsDao
import com.smartnotification.data.local.entity.HistoryEntity
import com.smartnotification.data.local.entity.NotificationEntity
import com.smartnotification.data.local.entity.PrioritySettingsEntity

@Database(
    entities = [
        NotificationEntity::class,
        HistoryEntity::class,
        PrioritySettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartNotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun historyDao(): HistoryDao
    abstract fun prioritySettingsDao(): PrioritySettingsDao
}
