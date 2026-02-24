package com.b096.dramarush5.ads

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.b096.dramarush5.R
import com.b096.dramarush5.ui.splash.SplashActivity

class HeadUpNotification {
    companion object {
        const val HEADER_CHANNEL_ID = "HEADER_CHANNEL_ID"
        const val BACKGROUND_NOTIFICATION_ID = 1111
        const val FOREGROUND_NOTIFICATION_ID = 2222
        const val HEADER_CHANNEL_NAME = "HEADER_CHANNEL_NAME"
        const val HEADER_CHANNEL_DESCRIPTION = "HEADER_CHANNEL_DESCRIPTION"

        @Volatile
        private var instance: HeadUpNotification? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: HeadUpNotification().also {
                    instance = it
                }
            }
    }

    fun onShowHomeOpen(context: Context): Notification {
        val isDarkMode =
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
        val textColor = if (isDarkMode)
            ContextCompat.getColor(context, R.color.white)
        else
            ContextCompat.getColor(context, R.color.black)

        val expandLayout =
            RemoteViews(context.packageName, R.layout.layout_notification_expand).apply {
                setTextColor(R.id.tvHeadLine, textColor)
                setTextColor(R.id.tvContent, textColor)
                setTextViewText(
                    R.id.tvHeadLine,
                    context.getString(R.string.tv_noti_header_home)
                )
                setTextViewText(R.id.tvContent, context.getString(R.string.tv_noti_content_home))
            }
        val collapseLayout =
            RemoteViews(context.packageName, R.layout.layout_notification_collapse).apply {
                setTextColor(R.id.tvHeadLine, textColor)
                setTextColor(R.id.tvContent, textColor)
                setTextViewText(R.id.tvHeadLine, context.getString(R.string.tv_noti_header_home))
                setTextViewText(R.id.tvContent, context.getString(R.string.tv_noti_content_home))
            }

        val notificationBuilder = NotificationCompat.Builder(context, HEADER_CHANNEL_ID)
            .setCustomContentView(collapseLayout)
            .setCustomHeadsUpContentView(expandLayout)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(context.resources.getColor(R.color.white))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(getHeadUpNotificationIntent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        return notificationBuilder.build()
    }

    fun onQuitNotiOpen(context: Context): Notification {
        val isDarkMode =
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
        val textColor = if (isDarkMode)
            ContextCompat.getColor(context, R.color.white)
        else
            ContextCompat.getColor(context, R.color.black)

        val expandLayout =
            RemoteViews(context.packageName, R.layout.layout_notification_expand).apply {
                setTextColor(R.id.tvHeadLine, textColor)
                setTextColor(R.id.tvContent, textColor)
                setTextViewText(
                    R.id.tvHeadLine,
                    context.getString(R.string.tv_noti_header_quit)
                )
                setTextViewText(R.id.tvContent, context.getString(R.string.tv_noti_content_quit))
            }
        val collapseLayout =
            RemoteViews(context.packageName, R.layout.layout_notification_collapse).apply {
                setTextColor(R.id.tvHeadLine, textColor)
                setTextColor(R.id.tvContent, textColor)
                setTextViewText(R.id.tvHeadLine, context.getString(R.string.tv_noti_header_quit))
                setTextViewText(R.id.tvContent, context.getString(R.string.tv_noti_content_quit))
            }
        val notificationBuilder = NotificationCompat.Builder(context, HEADER_CHANNEL_ID)
            .setCustomContentView(collapseLayout)
            .setCustomHeadsUpContentView(expandLayout)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setColor(context.resources.getColor(R.color.white))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(getHeadUpNotificationIntent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        return notificationBuilder.build()
    }

    private fun getHeadUpNotificationIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    fun createHeadUpNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(HEADER_CHANNEL_ID, HEADER_CHANNEL_NAME, importance).apply {
                    description = HEADER_CHANNEL_DESCRIPTION
                }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun clearNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}