package com.smartnotification.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.smartnotification.data.local.SmartNotificationDatabase
import com.smartnotification.data.local.dao.HistoryDao
import com.smartnotification.data.local.dao.NotificationDao
import com.smartnotification.data.local.dao.PrioritySettingsDao
import com.smartnotification.data.repository.NotificationRepository
import com.smartnotification.data.repository.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartNotificationDatabase {
        return Room.databaseBuilder(
            context,
            SmartNotificationDatabase::class.java,
            "smart_notification_db"
        ).build()
    }

    @Provides
    fun provideNotificationDao(db: SmartNotificationDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideHistoryDao(db: SmartNotificationDatabase): HistoryDao = db.historyDao()

    @Provides
    fun providePrioritySettingsDao(db: SmartNotificationDatabase): PrioritySettingsDao = db.prioritySettingsDao()
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}
