package com.b096.dramarush5.notilock

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.b096.dramarush5.notilock.NotifyLockScreenReceiver.Companion.REMINDER_CHANNEL_ID

class NotificationController {

    fun notify(
        context: Context,
        id: Int,
        build: Notification,
        channelId: String = REMINDER_CHANNEL_ID
    ) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                createReminderChannel(context, channelId)
                notify(id, build)
            }
        }
    }

    fun cancelMenuNotification(channelId: Int, context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(channelId)
        }
    }

    private fun createReminderChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val description = "Reminder"
            val channel = NotificationChannel(
                channelId, name, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = description
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}