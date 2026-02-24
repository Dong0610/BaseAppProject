package com.b096.dramarush5.notilock

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.b096.dramarush5.R

class NotifyLockScreenReceiver : BroadcastReceiver() {

    companion object {
        const val NOTILOCK_TAG ="TimeNotilock"
        const val REMINDER_CHANNEL_ID = "REMINDER_CHANNEL_ID"
    }

    private val notificationController by lazy { NotificationController() }

    private var reminderUtils: ReminderUtils? = null

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(NOTILOCK_TAG, "On receive Broadcast")
        if (reminderUtils == null) {
            reminderUtils = ReminderUtils()
            val keyguardManager =
                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            Log.d(NOTILOCK_TAG, "On Start New Time")
            if (!reminderUtils!!.isShow && (!(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive || keyguardManager.isKeyguardLocked)) {
                reminderUtils!!.isShow = true
                Log.d(NOTILOCK_TAG, "Noti lock show")
                val intent2 = Intent(context, NotifyLockScreenActivity::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                val extras = intent?.extras ?: Bundle()
                intent2.putExtras(extras)

                val activity = PendingIntent.getActivity(
                    context,
                    0,
                    intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                val fullScreenIntent = NotificationCompat.Builder(
                    context, REMINDER_CHANNEL_ID
                ).setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentText(context.getString(R.string.show_lock_screen))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(activity, true)
                notificationController.cancelMenuNotification(10000, context)
                val build = fullScreenIntent.build()
                notificationController.notify(context, 10000, build)
            } else {
                val keyguardManager =
                    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                val isLockscreen =  (!(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive || keyguardManager.isKeyguardLocked)
                Log.d(NOTILOCK_TAG, "Check Data: ${reminderUtils!!.isShow} -- $isLockscreen")
                if (!reminderUtils!!.isShow && isLockscreen) {
                    reminderUtils!!.isShow = true
                    Log.d(NOTILOCK_TAG, "Noti lock show")
                    val intent2 = Intent(context, NotifyLockScreenActivity::class.java)
                    intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    val extras = intent?.extras ?: Bundle()
                    intent2.putExtras(extras)

                    val activity = PendingIntent.getActivity(
                        context,
                        0,
                        intent2,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    val fullScreenIntent = NotificationCompat.Builder(
                        context, REMINDER_CHANNEL_ID
                    ).setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentText(context.getString(R.string.show_lock_screen))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setFullScreenIntent(activity, true)
                    notificationController.cancelMenuNotification(10000, context)
                    val build = fullScreenIntent.build()
                    notificationController.notify(context, 10000, build)
                }
            }
        }
    }
}