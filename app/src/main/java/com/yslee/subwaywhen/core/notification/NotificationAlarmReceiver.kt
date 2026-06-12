package com.yslee.subwaywhen.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yslee.subwaywhen.R
import java.util.concurrent.TimeUnit

class NotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: return
        val line = intent.getStringExtra(EXTRA_LINE) ?: ""
        val updnLine = intent.getStringExtra(EXTRA_UPDN_LINE) ?: ""
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        showNotification(context, stationName, line, updnLine)
        if (requestCode >= 0) rescheduleNextWeek(context, intent, requestCode)
    }

    private fun showNotification(context: Context, stationName: String, line: String, updnLine: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("지하철 출퇴근 알림")
            .setContentText("$stationName ($line $updnLine) 출발 시간이에요.")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(stationName.hashCode(), notification)
    }

    private fun rescheduleNextWeek(context: Context, originalIntent: Intent, requestCode: Int) {
        val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true
        if (!canSchedule) return

        val nextTriggerMs = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            originalIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerMs, pendingIntent)
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "출퇴근 알림",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_LINE = "extra_line"
        const val EXTRA_UPDN_LINE = "extra_updn_line"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val CHANNEL_ID = "work_alarm_channel"
    }
}
