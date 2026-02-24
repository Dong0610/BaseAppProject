package com.b096.dramarush5.notilock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.b096.dramarush5.app.remoteConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderUtils {

    companion object {
        const val REMINDER_ID = 1290
        val reminderUtils: ReminderUtils by lazy {
            ReminderUtils()
        }
    }

    var isShow = false

    fun createScheduleLockScreenReminder(
        context: Context?,
        broadcastReceiver: Class<out BroadcastReceiver> = NotifyLockScreenReceiver::class.java
    ) {
        if (context == null) return
        val listTimeRaw: List<Pair<Int, Int>> = remoteConfig.timeNotiLock
        if (listTimeRaw.isEmpty()) {
            Log.w("TimeNotiLock", "No reminder times configured.")
            return
        }
        val listTime = listTimeRaw
            .map { it.first.coerceIn(0, 23) to it.second.coerceIn(0, 59) }
            .distinct()
            .sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second })

        val now = Calendar.getInstance()
        val logFmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        listTime.forEachIndexed { index, (hour, minute) ->
            val nextCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) {
                    add(Calendar.DATE, 1)
                }
            }

            val intent = Intent(context, broadcastReceiver)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_ID + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = nextCal.timeInMillis
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
                Log.d("TimeNotiLock", "Exact reminder set at: ${logFmt.format(nextCal.time)}")
            } else {
                val windowLengthMs = 2 * 60 * 1000L
                alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    windowLengthMs,
                    pendingIntent
                )
                Log.w(
                    "TimeNotiLock",
                    "Exact alarm permission missing on S+. Scheduled inexact window at ~${
                        logFmt.format(
                            nextCal.time
                        )
                    }"
                )
            }
        }
    }
}