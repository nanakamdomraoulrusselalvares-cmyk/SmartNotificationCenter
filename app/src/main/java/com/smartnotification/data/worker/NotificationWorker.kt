package com.smartnotification.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartnotification.MainActivity
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
        val isAlarm = inputData.getBoolean(KEY_IS_ALARM, false)
        val priority = try { Priority.valueOf(priorityStr) } catch (e: Exception) { Priority.MEDIUM }

        // Post the system notification
        showNotification(title, message, priority, isAlarm)

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

    private fun showNotification(title: String, message: String, priority: Priority, isAlarm: Boolean) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when {
            isAlarm -> CHANNEL_ALARM
            priority == Priority.HIGH -> CHANNEL_HIGH
            priority == Priority.MEDIUM -> CHANNEL_MEDIUM
            else -> CHANNEL_LOW
        }

        // Ensure channels exist
        ensureChannels(nm)

        val importance = when {
            isAlarm || priority == Priority.HIGH -> NotificationCompat.PRIORITY_MAX
            priority == Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_MIN
        }

        // Create an Intent to open the app when clicking the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(importance)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)

        if (isAlarm) {
            // High priority alarm behavior
            builder.setFullScreenIntent(pendingIntent, true)
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            // Make it ring until dismissed (Insistent)
            builder.setOngoing(true)
            builder.setDefaults(Notification.DEFAULT_ALL)
        }

        val notification = builder.build()
        if (isAlarm) {
            // FLAG_INSISTENT makes the sound repeat until the user responds
            notification.flags = notification.flags or Notification.FLAG_INSISTENT or Notification.FLAG_SHOW_LIGHTS
        }

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannels(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = mutableListOf(
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
            )

            // Special Alarm Channel
            if (nm.getNotificationChannel(CHANNEL_ALARM) == null) {
                val alarmChannel = NotificationChannel(CHANNEL_ALARM, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Important alarms that ring even in DND"
                    enableVibration(true)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
                    setBypassDnd(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                channels.add(alarmChannel)
            }

            channels.forEach { nm.createNotificationChannel(it) }
        }
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_PRIORITY = "priority"
        const val KEY_IS_ALARM = "is_alarm"

        const val CHANNEL_HIGH = "channel_high"
        const val CHANNEL_MEDIUM = "channel_medium"
        const val CHANNEL_LOW = "channel_low"
        const val CHANNEL_ALARM = "channel_alarm"
    }
}
