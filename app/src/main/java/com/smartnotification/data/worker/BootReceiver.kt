package com.smartnotification.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.smartnotification.data.local.SmartNotificationDatabase
import com.smartnotification.domain.model.NotificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Reschedules all SCHEDULED notifications after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_REBOOT &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = androidx.room.Room.databaseBuilder(
                context,
                SmartNotificationDatabase::class.java,
                "smart_notification_db"
            ).build()

            val notifications = db.notificationDao().getScheduledNotifications().first()
            val now = System.currentTimeMillis()

            notifications.forEach { entity ->
                val delay = entity.scheduledTimeMillis - now
                if (delay > 0) {
                    val data = workDataOf(
                        NotificationWorker.KEY_NOTIFICATION_ID to entity.id,
                        NotificationWorker.KEY_TITLE to entity.title,
                        NotificationWorker.KEY_MESSAGE to entity.message,
                        NotificationWorker.KEY_PRIORITY to entity.priority,
                        NotificationWorker.KEY_IS_ALARM to entity.isAlarm
                    )
                    val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("notification_${entity.id}")
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "notification_${entity.id}",
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                    db.notificationDao().updateWorkerId(entity.id, request.id.toString())
                } else {
                    // Past scheduled time — mark as triggered
                    db.notificationDao().updateStatus(entity.id, NotificationStatus.TRIGGERED.name)
                }
            }
            db.close()
        }
    }
}
