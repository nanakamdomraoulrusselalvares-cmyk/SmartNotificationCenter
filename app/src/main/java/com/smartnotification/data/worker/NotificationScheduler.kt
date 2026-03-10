package com.smartnotification.data.worker

import androidx.work.*
import com.smartnotification.domain.model.NotificationItem
import com.smartnotification.domain.model.RepeatMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    /**
     * Schedule or reschedule a notification via WorkManager.
     * Returns the WorkRequest UUID string.
     */
    fun schedule(item: NotificationItem): String {
        val nowMillis = System.currentTimeMillis()
        val targetMillis = item.scheduledTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delay = (targetMillis - nowMillis).coerceAtLeast(0L)

        val data = workDataOf(
            NotificationWorker.KEY_NOTIFICATION_ID to item.id,
            NotificationWorker.KEY_TITLE to item.title,
            NotificationWorker.KEY_MESSAGE to item.message,
            NotificationWorker.KEY_PRIORITY to item.priority.name,
            NotificationWorker.KEY_IS_ALARM to item.isAlarm
        )

        val request = when (item.repeatMode) {
            RepeatMode.DAILY -> {
                PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("notification_${item.id}")
                    .build()
                    .also {
                        workManager.enqueueUniquePeriodicWork(
                            "notification_${item.id}",
                            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                            it
                        )
                    }
            }
            RepeatMode.WEEKLY -> {
                PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("notification_${item.id}")
                    .build()
                    .also {
                        workManager.enqueueUniquePeriodicWork(
                            "notification_${item.id}",
                            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                            it
                        )
                    }
            }
            RepeatMode.NONE -> {
                val builder = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("notification_${item.id}")
                
                if (item.isAlarm) {
                    builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                }

                builder.build().also {
                    workManager.enqueueUniqueWork(
                        "notification_${item.id}",
                        ExistingWorkPolicy.REPLACE,
                        it
                    )
                }
            }
        }

        return request.id.toString()
    }

    /**
     * Cancel a previously scheduled notification.
     */
    fun cancel(notificationId: Int) {
        workManager.cancelUniqueWork("notification_$notificationId")
    }
}
