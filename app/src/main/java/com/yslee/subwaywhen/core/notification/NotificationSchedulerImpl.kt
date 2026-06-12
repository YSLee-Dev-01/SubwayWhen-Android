package com.yslee.subwaywhen.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun reschedule(setting: SaveSetting, saveStations: List<SaveStation>) {
        cancelAll()

        val groupOneStation = saveStations.firstOrNull {
            it.id == setting.alertGroupOneId && it.group == SaveStationGroup.ONE
        }
        val groupTwoStation = saveStations.firstOrNull {
            it.id == setting.alertGroupTwoId && it.group == SaveStationGroup.TWO
        }

        val weekdays = if (setting.isWeekendNotificationEnabled) {
            listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        } else {
            listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
        }

        if (groupOneStation != null && setting.mainGroupOneTime != 0) {
            scheduleWeekly(
                station = groupOneStation,
                timeMinutes = setting.mainGroupOneTime,
                weekdays = weekdays,
                requestCodeBase = REQUEST_CODE_BASE_ONE,
            )
        }

        if (groupTwoStation != null && setting.mainGroupTwoTime != 0) {
            scheduleWeekly(
                station = groupTwoStation,
                timeMinutes = setting.mainGroupTwoTime,
                weekdays = weekdays,
                requestCodeBase = REQUEST_CODE_BASE_TWO,
            )
        }
    }

    private fun scheduleWeekly(
        station: SaveStation,
        timeMinutes: Int,
        weekdays: List<Int>,
        requestCodeBase: Int,
    ) {
        if (!canScheduleExactAlarms()) return

        val hour = timeMinutes / 60
        val minute = timeMinutes % 60

        weekdays.forEachIndexed { index, dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
                putExtra(NotificationAlarmReceiver.EXTRA_STATION_NAME, station.stationName)
                putExtra(NotificationAlarmReceiver.EXTRA_LINE, station.line)
                putExtra(NotificationAlarmReceiver.EXTRA_UPDN_LINE, station.updnLine)
                putExtra(NotificationAlarmReceiver.EXTRA_REQUEST_CODE, requestCodeBase + index)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCodeBase + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun cancelAll() {
        val allRequestCodes = (0 until 7).flatMap { i ->
            listOf(REQUEST_CODE_BASE_ONE + i, REQUEST_CODE_BASE_TWO + i)
        }
        allRequestCodes.forEach { code ->
            val intent = Intent(context, NotificationAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    companion object {
        private const val REQUEST_CODE_BASE_ONE = 1000
        private const val REQUEST_CODE_BASE_TWO = 2000
    }
}
