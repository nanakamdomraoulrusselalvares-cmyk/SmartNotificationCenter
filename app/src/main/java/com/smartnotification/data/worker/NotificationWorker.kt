package com.smartnotification.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartnotification.R
import com.smartnotification.data.repository.NotificationRepository
import com.smartnotification.domain.model.HistoryItem
import com.smartnotification.domain.model.NotificationStatus
import com.smartnotification.domain.model.Priority
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: NotificationRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, -1)
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val priorityStr = inputData.getString(KEY_PRIORITY) ?: Priority.MEDIUM.name
        val priority = try { Priority.valueOf(priorityStr) } catch (e: Exception) { Priority.MEDIUM }

        // Post the system notification
        showNotification(title, message, priority)

        // Update status in DB
        if (notificationId != -1) {
            repository.updateStatus(notificationId, NotificationStatus.TRIGGERED)
            repository.insertHistory(
                HistoryItem(
                    notificationId = notificationId,
                    title = title,
                    message = message,
                    triggeredAt = LocalDateTime.now(),
                    priority = priority
                )
            )
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String, priority: Priority) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when (priority) {
            Priority.HIGH -> CHANNEL_HIGH
            Priority.MEDIUM -> CHANNEL_MEDIUM
            Priority.LOW -> CHANNEL_LOW
        }

        // Ensure channels exist
        ensureChannels(nm)

        val importance = when (priority) {
            Priority.HIGH -> NotificationCompat.PRIORITY_MAX
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            Priority.LOW -> NotificationCompat.PRIORITY_MIN
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(importance)
            .setAutoCancel(true)

        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun ensureChannels(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(
                NotificationChannel(CHANNEL_HIGH, "High Priority", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Time-sensitive, heads-up notifications"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_MEDIUM, "Medium Priority", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Standard notifications"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_LOW, "Low Priority", NotificationManager.IMPORTANCE_MIN).apply {
                    description = "Silent background notifications"
                    enableVibration(false)
                    setSound(null, null)
                }
            ).forEach { nm.createNotificationChannel(it) }
        }
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_PRIORITY = "priority"

        const val CHANNEL_HIGH = "channel_high"
        const val CHANNEL_MEDIUM = "channel_medium"
        const val CHANNEL_LOW = "channel_low"
    }
}
