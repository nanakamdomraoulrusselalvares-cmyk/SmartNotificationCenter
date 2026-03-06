package com.smartnotification.domain.usecase

import com.smartnotification.data.repository.NotificationRepository
import com.smartnotification.data.worker.NotificationScheduler
import com.smartnotification.domain.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationItem>> = repository.getAllNotifications()
}

class GetScheduledNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationItem>> = repository.getScheduledNotifications()
}

class SearchNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(query: String): Flow<List<NotificationItem>> =
        if (query.isBlank()) repository.getAllNotifications()
        else repository.searchNotifications(query)
}

class CreateNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) {
    suspend operator fun invoke(item: NotificationItem): Result<Unit> {
        return try {
            val id = repository.insertNotification(item).toInt()
            val itemWithId = item.copy(id = id)
            val workerId = scheduler.schedule(itemWithId)
            repository.updateWorkerId(id, workerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UpdateNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) {
    suspend operator fun invoke(item: NotificationItem): Result<Unit> {
        return try {
            scheduler.cancel(item.id)
            repository.updateNotification(item.copy(status = NotificationStatus.SCHEDULED))
            val workerId = scheduler.schedule(item.copy(status = NotificationStatus.SCHEDULED))
            repository.updateWorkerId(item.id, workerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) {
    suspend operator fun invoke(item: NotificationItem): Result<Unit> {
        return try {
            scheduler.cancel(item.id)
            repository.deleteNotification(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CancelNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) {
    suspend operator fun invoke(item: NotificationItem): Result<Unit> {
        return try {
            scheduler.cancel(item.id)
            repository.updateStatus(item.id, NotificationStatus.CANCELLED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetHistoryUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<HistoryItem>> = repository.getAllHistory()
}

class SearchHistoryUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(query: String): Flow<List<HistoryItem>> =
        if (query.isBlank()) repository.getAllHistory()
        else repository.searchHistory(query)
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}

class GetPrioritySettingsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<PrioritySettings>> = repository.getAllPrioritySettings()
}

class UpdatePrioritySettingsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(settings: PrioritySettings) =
        repository.updatePrioritySettings(settings)
}
